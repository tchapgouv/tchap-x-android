/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.detection

import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import io.element.android.features.rageshake.api.R
import io.element.android.features.rageshake.api.screenshot.ImageResult
import io.element.android.features.rageshake.api.screenshot.screenshot
import io.element.android.libraries.androidutils.hardware.vibrate
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RageshakeDetectionView(
    state: RageshakeDetectionState,
    onOpenBugReport: () -> Unit = { },
) {
    val eventSink = state.eventSink
    val context = LocalContext.current

    // TCHAP : disable-screenshots
    AllowScreenshotEffect(state.takeScreenshot || state.showDialog)

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> eventSink(RageshakeDetectionEvent.StartDetection)
            Lifecycle.Event.ON_PAUSE -> eventSink(RageshakeDetectionEvent.StopDetection)
            else -> Unit
        }
    }
    when {
        state.takeScreenshot -> TakeScreenshot(
            onScreenshot = { eventSink(RageshakeDetectionEvent.ProcessScreenshot(it)) }
        )
        state.showDialog -> {
            LaunchedEffect(Unit) {
                context.vibrate()
            }
            RageshakeDialogContent(
                onNoClick = { eventSink(RageshakeDetectionEvent.Dismiss) },
                onDisableClick = { eventSink(RageshakeDetectionEvent.Disable) },
                onYesClick = onOpenBugReport
            )
        }
    }
}

// TCHAP : disable-screenshots
@Composable
private fun AllowScreenshotEffect(enabled: Boolean) {
    val activity = LocalActivity.current ?: return
    DisposableEffect(enabled) {
        if (enabled) {
            val window = activity.window
            val wasSecure = window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.setRecentsScreenshotEnabled(true)
            }
            onDispose {
                if (wasSecure) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        activity.setRecentsScreenshotEnabled(false)
                    }
                }
            }
        } else {
            onDispose {}
        }
    }
}

@Composable
private fun TakeScreenshot(
    onScreenshot: (ImageResult) -> Unit
) {
    val view = LocalView.current
    val latestOnScreenshot by rememberUpdatedState(onScreenshot)
    LaunchedEffect(Unit) {
        view.screenshot {
            latestOnScreenshot(it)
        }
    }
}

@Composable
private fun RageshakeDialogContent(
    onNoClick: () -> Unit = { },
    onDisableClick: () -> Unit = { },
    onYesClick: () -> Unit = { },
) {
    ConfirmationDialog(
        title = stringResource(id = CommonStrings.common_report_a_problem),
        content = stringResource(id = R.string.rageshake_detection_dialog_content),
        thirdButtonText = stringResource(id = CommonStrings.action_disable),
        submitText = stringResource(id = CommonStrings.action_yes),
        cancelText = stringResource(id = CommonStrings.action_no),
        onCancelClick = onNoClick,
        onThirdButtonClick = onDisableClick,
        onSubmitClick = onYesClick,
        onDismiss = onNoClick,
    )
}

@PreviewsDayNight
@Composable
internal fun RageshakeDialogContentPreview() = ElementPreview {
    RageshakeDialogContent()
}
