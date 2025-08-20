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
import android.util.Size
import androidx.annotation.UiThread
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.internal.TileServerStyleUriBuilder
import io.element.android.libraries.core.extensions.finally
import io.element.android.libraries.core.extensions.runCatchingExceptions
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.maplibre.android.snapshotter.MapSnapshotter
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class DefaultTchapMapRenderer(private val darkMode: Boolean, private val context: Context) : TchapMapRenderer {
    companion object {
        const val MAP_SNAPSHOT_SUBDIR: String = "tchap/map-snapshot-cache"
    }

    private val styleBuilder: Style.Builder = Style.Builder().fromUri(TileServerStyleUriBuilder().build(darkMode))
    private lateinit var mapSnapshotter: MapSnapshotter
    private val pendingGenerationSnapshot = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    init {
        MapLibre.getInstance(context)
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

    @UiThread
    override fun generateMapSnapshot(
        locationUiData: LocationUiData,
        onComplete: () -> Unit
    ) {
        val mapSnapshotFilename = getStaticMapFilenameFromLocation(locationUiData)
        val mapSnapshotFile = getStaticMapFileFromLocation(locationUiData)
        if (mapSnapshotFile.exists() && mapSnapshotFile.length() > 0 || pendingGenerationSnapshot.contains(mapSnapshotFilename)) {
            return
        }

        pendingGenerationSnapshot.add(mapSnapshotFilename)
        mapSnapshotter = getMapSnapshotter(context, locationUiData.location, locationUiData.mapZoom, locationUiData.mapSize)
        mapSnapshotter.start(
            { mapSnapshot ->
                runCatchingExceptions {
                    mapSnapshotFile.outputStream().use {
                        mapSnapshot.bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    onComplete()
                }.onFailure {
                    Timber.e("Map snapshot was not stored at this time")
                }.finally {
                    pendingGenerationSnapshot.remove(mapSnapshotFilename)
                }
            }
        )
    }

    private fun getMapSnapshotter(
        context: Context,
        location: Location,
        zoom: Double,
        size: Size
    ) = MapSnapshotter.Options(size.width, size.height)
        .withStyleBuilder(styleBuilder)
        .withCameraPosition(
            CameraPosition.Builder()
                .zoom(zoom)
                .target(LatLng(location.lat, location.lon))
                .build()
        )
        .let { MapSnapshotter(context, it) }
}
