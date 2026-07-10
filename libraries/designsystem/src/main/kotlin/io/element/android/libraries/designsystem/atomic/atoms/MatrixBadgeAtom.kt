/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.Badge
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=1960-491
 */
object MatrixBadgeAtom {
    data class MatrixBadgeData(
        val text: String,
        // :tchap: Optional for PrivateEncrypted "Suggested" badge
//        val icon: ImageVector,
        val icon: ImageVector? = null,
        // :tchap: end
        val type: Type,
    )

    enum class Type {
        // :tchap: New Custom Badge Types
        Warning,
        Default,
        // :tchap: end

        Positive,
        Neutral,
        Negative,
        Info,
    }

    @Composable
    fun View(
        data: MatrixBadgeData,
    ) {
        // :tchap: Custom Badge color
//        val backgroundColor = when (data.type) {
//            Type.Positive -> ElementTheme.colors.bgBadgeAccent
//            Type.Neutral -> ElementTheme.colors.bgBadgeDefault
//            Type.Negative -> ElementTheme.colors.bgCriticalSubtle
//            Type.Info -> ElementTheme.colors.bgBadgeInfo
//        }
//        val borderStroke = when (data.type) {
//            Type.Positive -> null
//            Type.Neutral -> BorderStroke(1.dp, ElementTheme.colors.borderInteractiveSecondary)
//            Type.Negative -> null
//            Type.Info -> null
//        }
//        val textColor = when (data.type) {
//            Type.Positive -> ElementTheme.colors.textBadgeAccent
//            Type.Neutral -> ElementTheme.colors.textPrimary
//            Type.Negative -> ElementTheme.colors.textCriticalPrimary
//            Type.Info -> ElementTheme.colors.textBadgeInfo
//        }
//        val iconColor = when (data.type) {
//            Type.Positive -> ElementTheme.colors.iconAccentPrimary
//            Type.Neutral -> ElementTheme.colors.iconPrimary
//            Type.Negative -> ElementTheme.colors.iconCriticalPrimary
//            Type.Info -> ElementTheme.colors.iconInfoPrimary
//        }
        val backgroundColor = when (data.type) {
            Type.Warning -> ElementTheme.colors.bgBadgeWarning
            Type.Default -> ElementTheme.colors.bgBadgeDefault
            Type.Positive -> ElementTheme.colors.bgBadgeSuccess
            Type.Neutral -> ElementTheme.colors.bgBadgeSecondary
            Type.Negative -> ElementTheme.colors.bgBadgeCritical
            Type.Info -> ElementTheme.colors.bgBadgeInfo
        }
        val borderStroke = when (data.type) {
            Type.Warning -> null
            Type.Default -> null
            Type.Positive -> null
            Type.Neutral -> null
            Type.Negative -> null
            Type.Info -> null
        }
        val textColor = when (data.type) {
            Type.Warning -> ElementTheme.colors.textBadgeWarning
            Type.Default -> ElementTheme.colors.textBadgeDefault
            Type.Positive -> ElementTheme.colors.textBadgeSuccess
            Type.Neutral -> ElementTheme.colors.textBadgeSecondary
            Type.Negative -> ElementTheme.colors.textBadgeCritical
            Type.Info -> ElementTheme.colors.textBadgeInfo
        }
        val iconColor = when (data.type) {
            Type.Warning -> ElementTheme.colors.iconBadgeWarning
            Type.Default -> ElementTheme.colors.iconBadgeDefault
            Type.Positive -> ElementTheme.colors.iconBadgeSuccess
            Type.Neutral -> ElementTheme.colors.iconBadgeSecondary
            Type.Negative -> ElementTheme.colors.iconBadgeCritical
            Type.Info -> ElementTheme.colors.iconBadgeInfo
        }
        // :tchap: end
        Badge(
            text = data.text.uppercase(),
            icon = data.icon,
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            textColor = textColor,
            isSmall = true,
            borderStroke = borderStroke,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomExternalPreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "External guests",
            icon = CompoundIcons.UserSolid(),
            type = MatrixBadgeAtom.Type.Warning,
        )
    )
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomPositivePreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "Trusted",
            icon = CompoundIcons.Verified(),
            type = MatrixBadgeAtom.Type.Positive,
        )
    )
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomNeutralPreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "Public room",
            icon = CompoundIcons.Public(),
            type = MatrixBadgeAtom.Type.Neutral,
        )
    )
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomNegativePreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "Not trusted",
            icon = CompoundIcons.ErrorSolid(),
            type = MatrixBadgeAtom.Type.Negative,
        )
    )
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomNeutralWrappingPreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "How much wood could a wood chuck chuck if a wood chuck could chuck wood",
            icon = CompoundIcons.LockOff(),
            type = MatrixBadgeAtom.Type.Info,
        )
    )
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomInfoPreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "Not encrypted",
            icon = CompoundIcons.LockOff(),
            type = MatrixBadgeAtom.Type.Info,
        )
    )
}
