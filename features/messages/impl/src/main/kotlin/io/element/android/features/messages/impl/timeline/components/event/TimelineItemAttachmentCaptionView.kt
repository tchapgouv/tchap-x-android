/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TimelineItemAttachmentCaptionView(
    caption: String,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = caption,
        color = ElementTheme.colors.textPrimary,
        style = ElementTheme.typography.fontBodyLgRegular,
        onTextLayout = ContentAvoidingLayout.measureLastTextLine(
            onContentLayoutChange = onContentLayoutChange,
        )
    )
}
