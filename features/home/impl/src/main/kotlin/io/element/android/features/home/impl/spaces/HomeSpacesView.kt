/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.ui.components.SpaceHeaderRootView
import io.element.android.libraries.matrix.ui.components.SpaceHeaderView
import io.element.android.libraries.matrix.ui.components.SpaceRoomItemView
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeSpacesView(
    state: HomeSpacesState,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    // TCHAP : Space default action is now conversation filtering
    onSpaceClick: (RoomId?) -> Unit,
    onTrailingSpaceClick: (RoomId) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.canCreateSpaces && state.spaceRooms.isEmpty()) {
        EmptySpaceHomeView(
            modifier = modifier.padding(contentPadding),
            onCreateSpaceClick = onCreateSpaceClick,
            onExploreClick = onExploreClick,
            canExploreSpaces = state.canExploreSpaces,
        )
    } else {
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
            contentPadding = contentPadding,
        ) {
            val space = state.space
            when (space) {
                CurrentSpace.Root -> {
                    item {
                        SpaceHeaderRootView(numberOfSpaces = state.spaceRooms.size)
                    }
                }
                is CurrentSpace.Space -> {
                    item {
                        SpaceHeaderView(
                            avatarData = space.spaceRoom.getAvatarData(AvatarSize.SpaceHeader),
                            alias = space.spaceRoom.canonicalAlias,
                            name = space.spaceRoom.displayName,
                            topic = space.spaceRoom.topic,
                            visibility = space.spaceRoom.visibility,
                            heroes = space.spaceRoom.heroes.toImmutableList(),
                            numberOfMembers = space.spaceRoom.numJoinedMembers,
                        )
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            // TCHAP : Space default action is now conversation filtering (add Home row)
            item {
                Row(
                    modifier = Modifier
                        .clickable(onClick = { onSpaceClick(null) })
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElementTheme.colors.bgSubtleSecondary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector = CompoundIcons.HomeSolid(),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            style = ElementTheme.typography.fontBodyLgMedium,
                            text = stringResource(R.string.tchap_spaces_filters_home_title),
                            color = ElementTheme.colors.textPrimary,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            style = ElementTheme.typography.fontBodyMdRegular,
                            text = stringResource(R.string.tchap_spaces_filters_home_description),
                            color = ElementTheme.colors.textSecondary,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (state.spaceRooms.size > 0) {
                    HorizontalDivider()
                }
            }

            itemsIndexed(
                items = state.spaceRooms,
                key = { _, spaceRoom -> spaceRoom.roomId }
            ) { index, spaceRoom ->
                val isInvitation = spaceRoom.state == CurrentUserMembership.INVITED
                SpaceRoomItemView(
                    spaceRoom = spaceRoom,
                    showUnreadIndicator = isInvitation && spaceRoom.roomId !in state.seenSpaceInvites,
                    hideAvatars = isInvitation && state.hideInvitesAvatar,
                    onClick = {
                        onSpaceClick(spaceRoom.roomId)
                    },
                    // TCHAP : Space default action is now conversation filtering (add trailing action)
                    trailingAction = {
                        IconButton(
                            modifier = Modifier.size(52.dp),
                            onClick = { onTrailingSpaceClick(spaceRoom.roomId) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = ElementTheme.colors.iconSecondary,
                            ),
                        ) {
                            Icon(
                                imageVector = CompoundIcons.Info(),
                                contentDescription = stringResource(CommonStrings.a11y_view_details)
                            )
                        }
                    },
                    onLongClick = {
                        // TODO
                    },
                )
                if (index != state.spaceRooms.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Ref: https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=1763-74215&t=9IGKMXHDfTGAqzQK-4
 */
@Composable
private fun EmptySpaceHomeView(
    onCreateSpaceClick: () -> Unit,
    onExploreClick: () -> Unit,
    canExploreSpaces: Boolean,
    modifier: Modifier = Modifier,
) {
    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp, start = 40.dp, end = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BigIcon(
                    style = BigIcon.Style.Default(CompoundIcons.SpaceSolid())
                )
                Text(
                    text = stringResource(CommonStrings.screen_space_list_empty_state_title),
                    style = ElementTheme.typography.fontHeadingLgBold,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }
        },
        footer = {
            ButtonColumnMolecule {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_create_space),
                    onClick = onCreateSpaceClick,
                )
                if (canExploreSpaces) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(CommonStrings.action_explore_public_spaces),
                        onClick = onExploreClick,
                    )
                }
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun HomeSpacesViewPreview(
    @PreviewParameter(HomeSpacesStateProvider::class) state: HomeSpacesState,
) = ElementPreview {
    HomeSpacesView(
        state = state,
        lazyListState = rememberLazyListState(),
        onSpaceClick = {},
        // TCHAP : Space default action is now conversation filtering (add trailing action)
        onTrailingSpaceClick = {},
        onCreateSpaceClick = {},
        onExploreClick = {},
        contentPadding = PaddingValues(bottom = 112.dp),
    )
}
