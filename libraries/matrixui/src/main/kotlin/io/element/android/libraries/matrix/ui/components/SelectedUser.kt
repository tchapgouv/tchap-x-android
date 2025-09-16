/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.gouv.tchap.libraries.tchaputils.TchapPatterns.isExternalTchapUser
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.badgeExternalBackgroundColor
import io.element.android.libraries.designsystem.theme.badgeExternalContentColor
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName

@Composable
fun SelectedUser(
    matrixUser: MatrixUser,
    canRemove: Boolean,
    onUserRemove: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectedItem(
        avatarData = matrixUser.getAvatarData(size = AvatarSize.SelectedUser),
        avatarType = AvatarType.User,
        text = matrixUser.getBestName(),
        maxLines = 2,
        a11yContentDescription = matrixUser.getBestName(),
        canRemove = canRemove,
        isExternalTchapUser =matrixUser.userId.toString().isExternalTchapUser(), // TCHAP external user
        onRemoveClick = { onUserRemove(matrixUser) },
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun SelectedUserExternalPreview(@PreviewParameter(MatrixUserWithAvatarProvider::class) user: MatrixUser) = ElementPreview {
    SelectedUser(
        aMatrixUser(displayName = "Guest", id = "@id_of_guest:e.server"),
        canRemove = true,
        onUserRemove = {},
    )
}

@PreviewsDayNight
@Composable
internal fun SelectedUserPreview(@PreviewParameter(MatrixUserWithAvatarProvider::class) user: MatrixUser) = ElementPreview {
    SelectedUser(
        matrixUser = user,
        canRemove = true,
        onUserRemove = {},
    )
}

@PreviewsDayNight
@Composable
internal fun SelectedUserRtlPreview() = CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
) {
    ElementPreview {
        SelectedUser(
            matrixUser = aMatrixUser(displayName = "John Doe"),
            canRemove = true,
            onUserRemove = {},
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SelectedUserCannotRemovePreview() = ElementPreview {
    SelectedUser(
        matrixUser = aMatrixUser(),
        canRemove = false,
        onUserRemove = {},
    )
}
