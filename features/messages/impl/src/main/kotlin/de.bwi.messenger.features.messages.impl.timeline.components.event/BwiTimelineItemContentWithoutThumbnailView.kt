/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package de.bwi.messenger.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.bwi.messenger.libraries.matrix.api.BwiContentScannerScanState
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemAttachmentCaptionView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentWithAttachment
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemFileContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import java.util.Locale

/*
Used for contents that don't have a preview, eg TimelineItemFileContent and TimelineItemAudioContent
*/
@Composable
fun BwiTimelineItemContentWithoutThumbnailView(
    content: TimelineItemEventContentWithAttachment,
    iconTrustedState: ImageVector,
    fileExtensionAndSize: String,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    var icon = iconTrustedState
    var iconTint = ElementTheme.materialColors.secondary
    var firstLine = content.filename
    var secondLine = fileExtensionAndSize
    when (content.scanState) {
        BwiContentScannerScanState.UNKNOWN,
        BwiContentScannerScanState.IN_PROGRESS -> {
            icon = CompoundIcons.Spinner()
            firstLine = content.filename
            secondLine = stringResource(R.string.screen_room_timeline_media_scanning)
        }

        BwiContentScannerScanState.TRUSTED -> {
            icon = iconTrustedState
            firstLine = content.filename
            secondLine = fileExtensionAndSize
        }

        BwiContentScannerScanState.INFECTED -> {
            icon = CompoundIcons.Block()
            iconTint = ElementTheme.colors.iconCriticalPrimary
            firstLine = content.filename
            secondLine = stringResource(R.string.screen_room_timeline_media_blocked)
                .capitalizeWithDefaultLocal()
        }

        BwiContentScannerScanState.NOT_FOUND -> {
            icon = CompoundIcons.FileError()
            firstLine = content.filename
            secondLine = stringResource(R.string.screen_room_timeline_media_not_available)
        }

        BwiContentScannerScanState.ERROR -> {
            icon = CompoundIcons.FileError()
            firstLine = content.filename
            secondLine = stringResource(R.string.screen_room_timeline_media_contentscanner_error)
        }
    }

    Column(
        modifier = modifier,
    ) {
        BwiTimelineItemAttachmentHeaderView(
            firstLine = firstLine,
            secondLine = secondLine,
            hasCaption = content.caption != null,
            onContentLayoutChange = onContentLayoutChange,
            icon = {
                if (content.scanState == BwiContentScannerScanState.IN_PROGRESS || content.scanState == BwiContentScannerScanState.UNKNOWN) {
                    CircularProgressIndicator(
                        color = iconTint,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(16.dp),
                    )
                }
            },
        )
        if (content.caption != null) {
            TimelineItemAttachmentCaptionView(
                modifier = Modifier.padding(top = 4.dp),
                caption = content.caption!!,
                onContentLayoutChange = onContentLayoutChange,
            )
        }
    }
}

private fun String.capitalizeWithDefaultLocal() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

@Preview
@Composable
internal fun InProgressFileViewPreview() = ElementPreview {
    BwiTimelineItemFileView(
        aTimelineItemFileContent().copy(scanState = BwiContentScannerScanState.IN_PROGRESS),
        onContentLayoutChange = {},
    )
}

@Preview
@Composable
internal fun TrustedFileViewPreview() = ElementPreview {
    BwiTimelineItemFileView(
        aTimelineItemFileContent(),
        onContentLayoutChange = {},
    )
}

@Preview
@Composable
internal fun TrustedWithCaptionFileViewPreview() = ElementPreview {
    BwiTimelineItemFileView(
        aTimelineItemFileContent(caption = "Das ist ein Caption!"),
        onContentLayoutChange = {},
    )
}

@Preview
@Composable
internal fun InfectedFileViewPreview() = ElementPreview {
    BwiTimelineItemFileView(
        aTimelineItemFileContent().copy(scanState = BwiContentScannerScanState.INFECTED),
        onContentLayoutChange = {},
    )
}

@Preview
@Composable
internal fun InfectedWithCaptionFileViewPreview() = ElementPreview {
    BwiTimelineItemFileView(
        aTimelineItemFileContent().copy(
            scanState = BwiContentScannerScanState.INFECTED,
            caption = "Das ist ein Caption!"
        ),
        onContentLayoutChange = {},
    )
}

@Preview
@Composable
internal fun NotFoundFileViewPreview() = ElementPreview {
    BwiTimelineItemFileView(
        aTimelineItemFileContent().copy(scanState = BwiContentScannerScanState.NOT_FOUND),
        onContentLayoutChange = {},
    )
}

@Preview
@Composable
internal fun ErrorFileViewPreview() = ElementPreview {
    BwiTimelineItemFileView(
        aTimelineItemFileContent().copy(scanState = BwiContentScannerScanState.ERROR),
        onContentLayoutChange = {},
    )
}
