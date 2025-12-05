/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.gouv.tchap.libraries.tchaputils.TchapPatterns.isExternalTchapUser
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.matrix.ui.components.CheckableUserRow
import io.element.android.libraries.matrix.ui.components.CheckableUserRowData
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.usersearch.api.UserSearchResult

@Composable
fun SearchMultipleUsersResultItem(
    showMatrixId: Boolean,
    searchResult: UserSearchResult,
    isUserSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = if (searchResult.isUnresolved) {
        CheckableUserRowData.Unresolved(
            avatarData = searchResult.matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = searchResult.matrixUser.userId.value,
        )
    } else {
        CheckableUserRowData.Resolved(
            name = searchResult.matrixUser.getBestName(),
            subtext = if (!showMatrixId || searchResult.matrixUser.displayName.isNullOrEmpty()) null else searchResult.matrixUser.userId.value,
            avatarData = searchResult.matrixUser.getAvatarData(AvatarSize.UserListItem),
            isExternalTchapUser = searchResult.matrixUser.userId.value.isExternalTchapUser(),
        )
    }
    CheckableUserRow(
        checked = isUserSelected,
        modifier = modifier,
        data = data,
        onCheckedChange = onCheckedChange,
    )
}

@Preview
@Composable
internal fun SearchMultipleUsersResultItemPreview() = ElementThemedPreview {
    Column {
        SearchMultipleUsersResultItem(
            showMatrixId = false,
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = false
            ),
            isUserSelected = false,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            showMatrixId = false,
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = false
            ),
            isUserSelected = true,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            showMatrixId = false,
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = true
            ),
            isUserSelected = false,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            showMatrixId = false,
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = true
            ),
            isUserSelected = true,
            onCheckedChange = {}
        )
    }
}
