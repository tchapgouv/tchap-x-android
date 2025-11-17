/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text

@Suppress("ModifierMissing")
@Composable
fun Badge(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    textColor: Color,
    iconColor: Color,
    shape: Shape = RoundedCornerShape(50),
    borderStroke: BorderStroke? = null,
    tintIcon: Boolean = true,
    isSmall: Boolean = false,
) {
    Surface(
        color = backgroundColor,
        contentColor = textColor,
        border = borderStroke,
        shape = if (isSmall) RoundedCornerShape(20) else shape,
    ) {
        val modifier = if (isSmall) {
            Modifier.padding(start = 2.dp, end = 4.dp, top = 1.dp, bottom = 1.dp)
        } else {
            Modifier.padding(start = 8.dp, end = 12.dp, top = 4.5.dp, bottom = 4.5.dp)
        }
        Row(
            modifier = modifier,
            horizontalArrangement = if (isSmall) Arrangement.spacedBy(2.dp) else Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = if (isSmall) Modifier.size(13.dp) else Modifier.size(16.dp),
                imageVector = icon,
                contentDescription = null,
                tint = if (tintIcon) iconColor else LocalContentColor.current,
            )
            Text(
                text = text,
                style = if (isSmall) ElementTheme.typography.fontBodyXsRegular else ElementTheme.typography.fontBodySmRegular,
                color = textColor,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun BadgePreview() {
    ElementPreview {
        Badge(
            text = "Trusted",
            icon = CompoundIcons.Verified(),
            backgroundColor = ElementTheme.colors.bgBadgeAccent,
            textColor = ElementTheme.colors.textBadgeAccent,
            iconColor = ElementTheme.colors.textBadgeAccent,
        )
    }
}
