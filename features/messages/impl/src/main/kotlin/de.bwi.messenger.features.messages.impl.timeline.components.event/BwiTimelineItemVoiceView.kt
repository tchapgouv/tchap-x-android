/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package de.bwi.messenger.features.messages.impl.timeline.components.event

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.bwi.messenger.libraries.matrix.api.BwiContentScannerScanState
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemVoiceView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.voiceplayer.api.VoiceMessageState

@Composable
fun BwiTimelineItemVoiceView(
    state: VoiceMessageState,
    content: TimelineItemVoiceContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (content.scanState == BwiContentScannerScanState.TRUSTED) {
        TimelineItemVoiceView(
            state = state,
            content = content,
            onContentLayoutChange = onContentLayoutChange,
            modifier = modifier
        )
    } else {
        BwiTimelineItemContentWithoutThumbnailView(
            content = content.copy(filename = stringResource(CommonStrings.common_voice_message)),
            iconTrustedState = Icons.Outlined.GraphicEq,
            fileExtensionAndSize = content.fileExtension,
            onContentLayoutChange = onContentLayoutChange,
            modifier = modifier,
        )
    }
}
