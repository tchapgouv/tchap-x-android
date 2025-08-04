/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package de.bwi.messenger.features.messages.impl.timeline.components.event

import android.text.SpannedString
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import de.bwi.messenger.libraries.matrix.api.BwiContentScannerScanState
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.event.DEFAULT_ASPECT_RATIO
import io.element.android.features.messages.impl.timeline.components.event.MAX_HEIGHT_IN_DP
import io.element.android.features.messages.impl.timeline.components.event.MIN_HEIGHT_IN_DP
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemAspectRatioBox
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentWithAttachment
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContentProvider
import io.element.android.features.messages.impl.timeline.protection.coerceRatioWhenHidingContent
import io.element.android.libraries.designsystem.components.blurhash.blurHashBackground
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.wysiwyg.compose.EditorStyledText
import io.element.android.wysiwyg.link.Link

@Composable
fun BwiTimelineItemContentView(
    content: TimelineItemEventContentWithAttachment,
    blurHash: String?,
    aspectRatio: Float?,
    onLinkClick: (Link) -> Unit,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
    containerModifier: Modifier = Modifier,
    description: String = "",
    trustedContent: (@Composable () -> Unit),
) {
    Column(
        modifier = modifier.semantics { contentDescription = description }
    ) {
        val timelineItemContainerModifier = if (content.scanState == BwiContentScannerScanState.TRUSTED) {
            containerModifier.blurHashBackground(blurHash, alpha = 0.9f)
        } else {
            containerModifier
        }

        TimelineItemAspectRatioBox(
            modifier = timelineItemContainerModifier,
            aspectRatio = coerceRatioWhenHidingContent(aspectRatio, content.scanState != BwiContentScannerScanState.TRUSTED),
            contentAlignment = Alignment.Center,
        ) {
            when (content.scanState) {
                BwiContentScannerScanState.TRUSTED -> {
                    trustedContent()
                }

                BwiContentScannerScanState.UNKNOWN,
                BwiContentScannerScanState.IN_PROGRESS -> {
                    BwiContentScannerInProgressContent(content.caption != null)
                }

                BwiContentScannerScanState.INFECTED -> {
                    BwiContentScannerInfectedContent(content)
                }

                BwiContentScannerScanState.NOT_FOUND -> {
                    BwiContentScannerNotAvailableContent(content.caption != null)
                }

                BwiContentScannerScanState.ERROR -> {
                    BwiContentScannerErrorContent(content.caption != null)
                }
            }
        }

        if (content.caption != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val caption = if (LocalInspectionMode.current) {
                SpannedString(content.caption)
            } else {
                content.formattedCaption ?: SpannedString(content.caption)
            }
            CompositionLocalProvider(
                LocalContentColor provides ElementTheme.colors.textPrimary,
                LocalTextStyle provides ElementTheme.typography.fontBodyLgRegular,
            ) {
                val contentAspectRatio = aspectRatio ?: DEFAULT_ASPECT_RATIO
                EditorStyledText(
                    modifier = Modifier
                        .padding(horizontal = 4.dp) // This is (12.dp - 8.dp) contentPadding from CommonLayout
                        .widthIn(min = MIN_HEIGHT_IN_DP.dp * contentAspectRatio, max = MAX_HEIGHT_IN_DP.dp * contentAspectRatio),
                    text = caption,
                    onLinkClickedListener = onLinkClick,
                    style = ElementRichTextEditorStyle.textStyle(),
                    releaseOnDetach = false,
                    onTextLayout = ContentAvoidingLayout.measureLegacyLastTextLine(onContentLayoutChange = onContentLayoutChange),
                )
            }
        }
    }
}

@Composable
fun BwiContentScannerInProgressContent(showCaption: Boolean) {
    BwiContentScannerNonTrustedContent(
        icon = CompoundIcons.Spinner(),
        text = R.string.screen_room_timeline_media_scanning,
        showCaption = showCaption,
        isScanning = true
    )
}

@Composable
fun BwiContentScannerNotAvailableContent(showCaption: Boolean) {
    BwiContentScannerNonTrustedContent(
        icon = CompoundIcons.FileError(),
        text = R.string.screen_room_timeline_media_not_available,
        showCaption = showCaption,
    )
}

@Composable
fun BwiContentScannerErrorContent(showCaption: Boolean) {
    BwiContentScannerNonTrustedContent(
        icon = CompoundIcons.FileError(),
        text = R.string.screen_room_timeline_media_contentscanner_error,
        showCaption = showCaption,
    )
}

@Composable
private fun BwiContentScannerNonTrustedContent(icon: ImageVector, @StringRes text: Int, showCaption: Boolean, isScanning: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isScanning) {
            CircularProgressIndicator(
                color = ElementTheme.materialColors.secondary,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            IconContent(
                imageVector = icon,
                contentDescription = stringResource(text),
                tint = ElementTheme.materialColors.secondary,
            )
        }

        TextContent(showCaption, text)
    }
}

@Composable
fun BwiContentScannerInfectedContent(content: TimelineItemEventContentWithAttachment) {
    Column {
        IconContent(
            imageVector = CompoundIcons.Block(),
            contentDescription = stringResource(R.string.screen_room_timeline_media_blocked),
            tint = ElementTheme.colors.iconCriticalPrimary,
        )

        val verticalPadding =
            if (content.caption != null) {
                0.dp
            } else {
                25.dp
            }
        Row(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(horizontal = 25.dp, vertical = verticalPadding),
        ) {
            Text(
                text = content.filename.substringBefore("."),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            val extension = ".${content.fileExtension}"
            Text(
                text = extension,
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Visible,
            )

            val blocked = " ${stringResource(R.string.screen_room_timeline_media_blocked)}"
            Text(
                text = blocked,
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.materialColors.primary,
                maxLines = 1,
                overflow = TextOverflow.Visible,
            )
        }
    }
}

@Composable
private fun ColumnScope.TextContent(showCaption: Boolean, @StringRes stringId: Int) {
    val verticalPadding =
        if (showCaption) {
            0.dp
        } else {
            25.dp
        }
    Row(
        modifier = Modifier
            .align(alignment = Alignment.CenterHorizontally)
            .padding(horizontal = 25.dp, vertical = verticalPadding),
    ) {
        Text(
            text = stringResource(stringId),
            style = ElementTheme.typography.fontBodyXsRegular,
            color = ElementTheme.colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ColumnScope.IconContent(
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .align(alignment = Alignment.CenterHorizontally),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .background(ElementTheme.colors.bgCanvasDefault, shape = CircleShape)
                .padding(horizontal = 5.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = tint,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemInfectedContentPreview(
    @PreviewParameter(
        TimelineItemVideoContentProvider::class
    ) content: TimelineItemVideoContent
) = ElementPreview {
    BwiContentScannerInfectedContent(content.copy(scanState = BwiContentScannerScanState.INFECTED))
}

@PreviewsDayNight
@Composable
internal fun TimelineItemInProgressContentPreview() = ElementPreview {
    BwiContentScannerInProgressContent(false)
}

@PreviewsDayNight
@Composable
internal fun TimelineItemNotAvailableContentPreview() = ElementPreview {
    BwiContentScannerNotAvailableContent(false)
}

@PreviewsDayNight
@Composable
internal fun TimelineItemErrorContentPreview() = ElementPreview {
    BwiContentScannerErrorContent(false)
}
