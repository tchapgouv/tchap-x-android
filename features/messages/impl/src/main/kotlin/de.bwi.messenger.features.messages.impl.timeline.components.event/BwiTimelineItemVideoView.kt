/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package de.bwi.messenger.features.messages.impl.timeline.components.event

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import de.bwi.messenger.libraries.matrix.api.BwiContentScannerScanState
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.components.ATimelineItemEventRow
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContentProvider
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.protection.ProtectedView
import io.element.android.libraries.designsystem.modifiers.roundedBackground
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.MAX_THUMBNAIL_HEIGHT
import io.element.android.libraries.matrix.ui.media.MAX_THUMBNAIL_WIDTH
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.matrix.ui.media.MediaRequestData.Kind.Thumbnail
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BwiTimelineItemVideoView(
    content: TimelineItemVideoContent,
    hideMediaContent: Boolean,
    onContentClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onShowContentClick: () -> Unit,
    onLinkClick: (String) -> Unit,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerModifier = if (content.showCaption) {
        Modifier
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(6.dp))
    } else {
        Modifier
    }
    BwiTimelineItemContentView(
        content = content,
        blurHash = content.blurHash,
        aspectRatio = content.aspectRatio,
        onLinkClick = onLinkClick,
        onContentLayoutChange = onContentLayoutChange,
        modifier = modifier,
        containerModifier = containerModifier,
        description = stringResource(CommonStrings.common_video),
    ) {
        TrustedVideoContent(
            hideMediaContent = hideMediaContent,
            onContentClick = onContentClick,
            onLongClick = onLongClick,
            onShowContentClick = onShowContentClick,
            content = content
        )
    }
}

@Composable
@ExperimentalFoundationApi
private fun TrustedVideoContent(
    hideMediaContent: Boolean,
    onContentClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onShowContentClick: () -> Unit,
    content: TimelineItemVideoContent,
) {
    ProtectedView(
        hideContent = hideMediaContent,
        onShowClick = onShowContentClick,
    ) {
        var isLoaded by remember { mutableStateOf(false) }
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isLoaded) Modifier.background(Color.White) else Modifier)
                .then(if (onContentClick != null) Modifier.combinedClickable(onClick = onContentClick, onLongClick = onLongClick) else Modifier),
            model = MediaRequestData(
                source = content.thumbnailSource,
                kind = Thumbnail(
                    width = content.thumbnailWidth?.toLong() ?: MAX_THUMBNAIL_WIDTH,
                    height = content.thumbnailHeight?.toLong() ?: MAX_THUMBNAIL_HEIGHT,
                )
            ),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
            contentDescription = null,
            onState = { isLoaded = it is AsyncImagePainter.State.Success },
        )

        Box(
            modifier = Modifier.roundedBackground(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                Icons.Default.PlayArrow,
                contentDescription = stringResource(id = CommonStrings.a11y_play),
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVideoViewPreview(@PreviewParameter(TimelineItemVideoContentProvider::class) content: TimelineItemVideoContent) = ElementPreview {
    BwiTimelineItemVideoView(
        content = content,
        hideMediaContent = false,
        onShowContentClick = {},
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemInfectedVideoViewPreview(
    @PreviewParameter(
        TimelineItemVideoContentProvider::class
    ) content: TimelineItemVideoContent
) = ElementPreview {
    BwiTimelineItemVideoView(
        content = content.copy(
            scanState = BwiContentScannerScanState.INFECTED,
            filename = "veeeeeeerrrrryyyylongfilename.mp4"
        ),
        hideMediaContent = false,
        onShowContentClick = {},
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVideoViewHideMediaContentPreview() = ElementPreview {
    BwiTimelineItemVideoView(
        content = aTimelineItemVideoContent(),
        hideMediaContent = true,
        onShowContentClick = {},
        onContentClick = {},
        onLongClick = {},
        onLinkClick = {},
        onContentLayoutChange = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineVideoWithCaptionRowPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemVideoContent().copy(
                        filename = "video.mp4",
                        caption = "A long caption that may wrap into several lines",
                        aspectRatio = 2.5f,
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = false,
                content = aTimelineItemVideoContent().copy(
                    filename = "video.mp4",
                    caption = "Video with null aspect ratio",
                    aspectRatio = null,
                ),
                groupPosition = TimelineItemGroupPosition.Last,
            ),
        )
    }
}
