/*
 * MIT License
 *
 * Copyright (c) 2025. DINUM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.gouv.tchap.android.features.location.api

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import io.element.android.features.location.api.buildTimeMapTilerConfig
import io.element.android.features.location.api.internal.MapTilerTileServerStyleUriBuilder
import io.element.android.libraries.core.extensions.runCatchingExceptions
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MapRuntimeOptions
import org.maplibre.compose.map.MapSnapshotRequest
import org.maplibre.compose.map.createMapRuntime
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class DefaultTchapMapRenderer(private val darkMode: Boolean, private val context: Context) : TchapMapRenderer {
    companion object {
        private const val MAP_SNAPSHOT_SUBDIR = "tchap/map-snapshot-cache"
        private const val MAP_SNAPSHOT_MAX_AGE_DAYS = 30L

        private var hasCleanedSnapshots = false

        // Limit the number of concurrent snapshots to avoid crashing Vulkan/GPU resources,
        // especially on emulators or low-end devices.
        private val snapshotSemaphore = Semaphore(4)

        private fun cleanOldSnapshots(context: Context) {
            if (hasCleanedSnapshots) return
            hasCleanedSnapshots = true
            thread(name = "TchapMapSnapshotCacheCleaner") {
                runCatchingExceptions {
                    val snapshotDir = File(context.applicationContext.cacheDir, MAP_SNAPSHOT_SUBDIR)
                    if (!snapshotDir.exists() || !snapshotDir.isDirectory) return@runCatchingExceptions

                    val now = System.currentTimeMillis()
                    val retentionMillis = TimeUnit.DAYS.toMillis(MAP_SNAPSHOT_MAX_AGE_DAYS)

                    snapshotDir.listFiles()?.forEach { file ->
                        if (now - file.lastModified() > retentionMillis) {
                            file.delete()
                        }
                    }
                }
            }
        }
    }

    private val pendingGenerationSnapshot = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    init {
        cleanOldSnapshots(context)
    }

    private fun hashString(input: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }

    private fun getStaticMapFilenameFromLocation(locationUiData: LocationUiData): String {
        val mapModeText = if (darkMode) "dark" else "light"

        return hashString("$locationUiData$mapModeText")
    }

    override fun getStaticMapFileFromLocation(locationUiData: LocationUiData): File {
        val snapshotDir = File(context.applicationContext.cacheDir, MAP_SNAPSHOT_SUBDIR)

        if (!snapshotDir.exists()) snapshotDir.mkdirs()

        return File(snapshotDir, getStaticMapFilenameFromLocation(locationUiData))
    }

    override suspend fun generateMapSnapshot(
        locationUiData: LocationUiData
    ) {
        val mapSnapshotFilename = getStaticMapFilenameFromLocation(locationUiData)
        val mapSnapshotFile = getStaticMapFileFromLocation(locationUiData)
        if (mapSnapshotFile.exists() && mapSnapshotFile.length() > 0 || pendingGenerationSnapshot.contains(mapSnapshotFilename)) {
            return
        }

        pendingGenerationSnapshot.add(mapSnapshotFilename)

        try {
            snapshotSemaphore.withPermit {
                runCatchingExceptions {
                    val mapRuntime = createMapRuntime(MapRuntimeOptions())
                    val mapTilerConfig = buildTimeMapTilerConfig() ?: error("Missing MapTiler API Key")
                    val mapSnapshotter = mapRuntime.createSnapshotter(
                        baseStyle = BaseStyle.Uri(MapTilerTileServerStyleUriBuilder().build(mapTilerConfig, darkMode)),
                        content = {}
                    )

                    val imageBitmap = mapSnapshotter.capture(
                        MapSnapshotRequest(
                            width = locationUiData.mapSize.width,
                            height = locationUiData.mapSize.height,
                            cameraPosition = CameraPosition(
                                target = Position(locationUiData.location.lon, locationUiData.location.lat),
                                zoom = locationUiData.mapZoom
                            )
                        )
                    )
                    
                    val bitmap = imageBitmap.asAndroidBitmap()
                    mapSnapshotFile.outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    
                    mapSnapshotter.close()
                    mapRuntime.close()
                }.onFailure {
                    Timber.e(it, "Error during map snapshot generation")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during map snapshot generation")
        } finally {
            pendingGenerationSnapshot.remove(mapSnapshotFilename)
        }
    }
}
