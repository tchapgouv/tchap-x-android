/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import fr.gouv.tchap.libraries.tchaputils.TchapPatterns
import fr.gouv.tchap.libraries.tchaputils.TchapPatterns.isExternalTchapUser
import io.element.android.features.invitepeople.api.InvitePeopleEvents
import io.element.android.features.invitepeople.api.InvitePeoplePresenter
import io.element.android.features.invitepeople.api.InvitePeopleState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.map
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
<<<<<<< HEAD
import io.element.android.libraries.matrix.api.createroom.RoomAccessRules
=======
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.createroom.RoomPreset
>>>>>>> main-element
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.filterMembers
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.recent.getRecentDirectRooms
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.usersearch.api.UserRepository
import io.element.android.services.apperror.api.AppErrorStateService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_SUGGESTIONS_COUNT = 5

@AssistedInject
class DefaultInvitePeoplePresenter(
    @Assisted private val joinedRoom: JoinedRoom?,
    @Assisted private val roomId: RoomId,
    private val featureFlagService: FeatureFlagService,
    private val userRepository: UserRepository,
    private val coroutineDispatchers: CoroutineDispatchers,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val appErrorStateService: AppErrorStateService,
    private val matrixClient: MatrixClient,
) : InvitePeoplePresenter {
    @AssistedFactory
    @ContributesBinding(SessionScope::class)
    interface Factory : InvitePeoplePresenter.Factory {
        override fun create(joinedRoom: JoinedRoom?, roomId: RoomId): DefaultInvitePeoplePresenter
    }

    @Composable
    override fun present(): InvitePeopleState {
        val roomMembers = remember { mutableStateOf<AsyncData<ImmutableList<RoomMember>>>(AsyncData.Loading()) }
        val selectedUsers = remember { mutableStateOf<ImmutableList<MatrixUser>>(persistentListOf()) }
        val searchResults = remember { mutableStateOf<SearchBarResultState<ImmutableList<InvitableUser>>>(SearchBarResultState.Initial()) }
        val queryState = rememberTextFieldState()
        var searchActive by rememberSaveable { mutableStateOf(false) }
        val showSearchLoader = rememberSaveable { mutableStateOf(false) }
        var showOpenRoomToExternalsDialog by rememberSaveable { mutableStateOf(false) } // TCHAP external user
        val sendInvitesAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        val createRoomFromDmAction = remember { mutableStateOf<AsyncAction<RoomId>>(AsyncAction.Uninitialized) }

        val showMatrixId by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.ShowMatrixId)
        }.collectAsState(false)

        val recentDirectRooms by produceState(emptyList(), roomMembers.value) {
            if (roomMembers.value.isSuccess()) {
                val activeMemberIds = roomMembers.value.dataOrNull().orEmpty()
                    .filter { it.membership.isActive() }
                    .mapTo(mutableSetOf()) { it.userId }

                value = matrixClient.getRecentDirectRooms()
                    .filterNot { it.matrixUser.userId in activeMemberIds }
                    .take(MAX_SUGGESTIONS_COUNT)
                    .toList()
            }
        }

        // Convert recent direct rooms to InvitableUser for display
        val suggestions by remember {
            derivedStateOf {
                recentDirectRooms.map { recentDirectRoom ->
                    InvitableUser(
                        matrixUser = recentDirectRoom.matrixUser,
                        isSelected = recentDirectRoom.matrixUser in selectedUsers.value,
                        isAlreadyJoined = false,
                        isAlreadyInvited = false,
                        isUnresolved = false,
                    )
                }.toImmutableList()
            }
        }

        val room by produceState(if (joinedRoom != null) AsyncData.Success(joinedRoom) else AsyncData.Loading()) {
            if (joinedRoom == null) {
                val result = matrixClient.getJoinedRoom(roomId)
                value = if (result == null) {
                    AsyncData.Failure(Exception("Room not found"))
                } else {
                    AsyncData.Success(result)
                }
            }
        }

        val selectedUserIdentities = produceState(
            emptyMap<MatrixUser, IdentityState?>().toImmutableMap(),
            selectedUsers.value,
        ) {
            val selected = selectedUsers.value

            val cached = value
                .filterKeys { it in selected }

            val uncached = selected
                .filterNot(cached::containsKey)
                .associateWith { user ->
                    matrixClient.encryptionService
                        .getUserIdentity(user.userId, fallbackToServer = false)
                        .getOrNull()
                }

            value = (cached + uncached).toImmutableMap()
        }

        val unknownUsers by remember {
            derivedStateOf {
                selectedUserIdentities.value
                    .filterValues { it == null }
                    .keys
                    .toImmutableList()
            }
        }

        LaunchedEffect(room.isSuccess()) {
            room.dataOrNull()?.let {
                fetchMembers(it, roomMembers)
            }
        }
        val searchQuery = queryState.text.toString()
        LaunchedEffect(searchQuery, roomMembers) {
            performSearch(
                searchResults = searchResults,
                roomMembers = roomMembers,
                selectedUsers = selectedUsers,
                showSearchLoader = showSearchLoader,
                searchQuery = searchQuery
            )
        }

        fun handleEvent(event: InvitePeopleEvents) {
            when (event) {
                // Dedicated `when` for exhaustivity.
                is DefaultInvitePeopleEvents -> when (event) {
                    is DefaultInvitePeopleEvents.OnSearchActiveChanged -> {
                        searchActive = event.active
                        if (!event.active) {
                            queryState.clearText()
                        }
                    }

                    is DefaultInvitePeopleEvents.ToggleUser -> {
                        selectedUsers.toggleUser(event.user)
                        searchResults.toggleUser(event.user)
                        // suggestions will automatically update via derivedStateOf when selectedUsers changes
                    }
                    is DefaultInvitePeopleEvents.DismissUnknownUsersModal -> {
                        sendInvitesAction.value = AsyncAction.Uninitialized
                    }
                    is DefaultInvitePeopleEvents.RemoveUnknownUsers -> {
                        val usersToRemove = selectedUsers.value.filter { it in unknownUsers }
                        usersToRemove.forEach { user ->
                            selectedUsers.toggleUser(user)
                            searchResults.toggleUser(user)
                        }
                        sendInvitesAction.value = AsyncAction.Uninitialized
                    }
                }
                is InvitePeopleEvents.SendInvites -> {
                    if (unknownUsers.isNotEmpty() && sendInvitesAction.value !is ConfirmingUnknownUserInvitation) {
                        sendInvitesAction.value = ConfirmingUnknownUserInvitation(
                            unknownUsers
                        )
                    } else {
<<<<<<< HEAD
                        showOpenRoomToExternalsDialog = false // TCHAP external user
                        // TCHAP invite-by-email : call sendInvites or sendTchapEmailInvites
                        // depending if the user need to be invited by email to create an account
//                        room.dataOrNull()?.let {
//                            sessionCoroutineScope.sendInvites(it, selectedUsers.value, sendInvitesAction)
                        room.dataOrNull()?.let { room ->
                            val (emailInvites, userInvites) = selectedUsers.value.partition {
                                it.userId.value.contains(TchapPatterns.inviteByEmailSuffixMarker())
                            }

                            if (userInvites.isNotEmpty()) {
                                sessionCoroutineScope.sendInvites(room, userInvites, sendInvitesAction)
                            }
                            if (emailInvites.isNotEmpty()) {
                                sessionCoroutineScope.sendTchapEmailInvites(room, emailInvites, sendInvitesAction)
=======
                        room.dataOrNull()?.let {
                            sessionCoroutineScope.launch {
                                if (it.isDm()) {
                                    createRoomFromDm(it, selectedUsers.value, createRoomFromDmAction)
                                } else {
                                    sendInvites(it, selectedUsers.value, sendInvitesAction)
                                }
>>>>>>> main-element
                            }
                        }
                    }
                }
                is InvitePeopleEvents.CloseSearch -> {
                    searchActive = false
                    queryState.clearText()
                }
<<<<<<< HEAD
                // TCHAP external user
                is InvitePeopleEvents.CheckExternalsAndSendInvites -> {
                    val hasSelectedExternalUsers = selectedUsers.value.any { it.userId.toString().isExternalTchapUser() }
                    if (hasSelectedExternalUsers && !room.dataOrNull()?.info()?.isOpenToExternalUsers!!) {
                        showOpenRoomToExternalsDialog = true
                    } else {
                        handleEvent(InvitePeopleEvents.SendInvites)
                    }
=======
                is InvitePeopleEvents.ClearError -> {
                    sendInvitesAction.value = AsyncAction.Uninitialized
                    createRoomFromDmAction.value = AsyncAction.Uninitialized
>>>>>>> main-element
                }
            }
        }

        // TCHAP external user
        if (showOpenRoomToExternalsDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.screen_invite_confirm_open_room_to_externals_title),
                content = stringResource(R.string.screen_invite_confirm_open_room_to_externals_message),
                onSubmitClick = {
                    room.dataOrNull()?.let {
                        sessionCoroutineScope.launch {
                            it.setAccessRule(RoomAccessRules.UNRESTRICTED)
                                .onSuccess {
                                    handleEvent(InvitePeopleEvents.SendInvites)
                                }
                                .onFailure {
                                    showOpenRoomToExternalsDialog = false
                                    appErrorStateService.showError(
                                        titleRes = R.string.screen_invite_unable_to_open_room_to_externals_title,
                                        bodyRes = R.string.screen_invite_unable_to_open_room_to_externals_message,
                                    )
                                }
                        }
                    }
                },
                onDismiss = {
                    showOpenRoomToExternalsDialog = false
                }
            )
        }

        return DefaultInvitePeopleState(
            room = room.map { },
            showMatrixId = showMatrixId,
            canInvite = selectedUsers.value.isNotEmpty() && !sendInvitesAction.value.isLoading(),
            selectedUsers = selectedUsers.value,
            searchQuery = queryState,
            isSearchActive = searchActive,
            searchResults = searchResults.value,
            showSearchLoader = showSearchLoader.value,
            sendInvitesAction = sendInvitesAction.value,
            createRoomFromDmAction = createRoomFromDmAction.value,
            suggestions = suggestions,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.sendInvites(
        room: JoinedRoom,
        selectedUsers: List<MatrixUser>,
        sendInvitesAction: MutableState<AsyncAction<Unit>>,
    ) = launch {
        sendInvitesAction.runUpdatingState {
            val anyInviteFailed = selectedUsers
                .map { room.inviteUserById(it.userId) }
                .any { it.isFailure }

            if (anyInviteFailed) {
                appErrorStateService.showError(
                    titleRes = CommonStrings.common_unable_to_invite_title,
                    bodyRes = CommonStrings.common_unable_to_invite_message,
                )
            }

            Result.success(Unit)
        }
    }

<<<<<<< HEAD
    // TCHAP invite-by-email : send an invite by email to create a Tchap account for all email in emailToInvite
    private fun CoroutineScope.sendTchapEmailInvites(
        room: JoinedRoom,
        emailToInvite: List<MatrixUser>,
        sendInvitesAction: MutableState<AsyncAction<Unit>>,
    ) = launch {
        sendInvitesAction.runUpdatingState {
            val anyInviteFailed = room.inviteUsersByEmail(
                emailToInvite.map { it.displayName!! }
            )

            if (anyInviteFailed.isFailure) {
                appErrorStateService.showError(
                    titleRes = CommonStrings.common_unable_to_invite_title,
                    bodyRes = CommonStrings.common_unable_to_invite_message,
                )
            }

            Result.success(Unit)
=======
    private fun CoroutineScope.createRoomFromDm(
        currentRoom: JoinedRoom,
        selectedUsers: List<MatrixUser>,
        createRoomFromDmAction: MutableState<AsyncAction<RoomId>>,
    ) = launch {
        createRoomFromDmAction.runUpdatingState {
            val currentUsers = currentRoom.getMembers(limit = 100).getOrNull().orEmpty()
                .filter { it.membership.isActive() }
            val invitees = (currentUsers.map { it.userId } + selectedUsers.map { it.userId })
                .filter { it != matrixClient.sessionId }
                .distinct()
            matrixClient.createRoom(
                CreateRoomParameters(
                    name = null,
                    topic = null,
                    isEncrypted = true,
                    isDirect = false,
                    visibility = RoomVisibility.Private,
                    preset = RoomPreset.PRIVATE_CHAT,
                    invite = invitees,
                    avatar = null,
                    joinRuleOverride = JoinRule.Invite,
                    historyVisibilityOverride = RoomHistoryVisibility.Invited,
                    isSpace = false,
                )
            )
>>>>>>> main-element
        }
    }

    @JvmName("toggleUserInSelectedUsers")
    private fun MutableState<ImmutableList<MatrixUser>>.toggleUser(user: MatrixUser) {
        value = if (value.contains(user)) {
            value.filterNot { it.userId == user.userId }
        } else {
            value + user
        }.toImmutableList()
    }

    @JvmName("toggleUserInSearchResults")
    private fun MutableState<SearchBarResultState<ImmutableList<InvitableUser>>>.toggleUser(user: MatrixUser) {
        val existingResults = value
        if (existingResults is SearchBarResultState.Results) {
            value = SearchBarResultState.Results(
                existingResults.results.map { iu ->
                    if (iu.matrixUser == user) {
                        iu.copy(isSelected = !iu.isSelected)
                    } else {
                        iu
                    }
                }.toImmutableList()
            )
        }
    }

    private suspend fun performSearch(
        searchResults: MutableState<SearchBarResultState<ImmutableList<InvitableUser>>>,
        roomMembers: MutableState<AsyncData<ImmutableList<RoomMember>>>,
        selectedUsers: MutableState<ImmutableList<MatrixUser>>,
        showSearchLoader: MutableState<Boolean>,
        searchQuery: String,
    ) = withContext(coroutineDispatchers.io) {
        searchResults.value = SearchBarResultState.Initial()
        showSearchLoader.value = false
        val joinedMembers = roomMembers.value.dataOrNull().orEmpty()

        userRepository.search(searchQuery).onEach { state ->
            showSearchLoader.value = state.isSearching
            searchResults.value = when {
                state.results.isEmpty() && state.isSearching -> SearchBarResultState.Initial()
                state.results.isEmpty() && !state.isSearching -> SearchBarResultState.NoResultsFound()
                else -> SearchBarResultState.Results(state.results.map { result ->
                    val existingMembership = joinedMembers.firstOrNull { j -> j.userId == result.matrixUser.userId }?.membership
                    val isJoined = existingMembership == RoomMembershipState.JOIN
                    val isInvited = existingMembership == RoomMembershipState.INVITE
                    InvitableUser(
                        matrixUser = result.matrixUser,
                        isSelected = selectedUsers.value.contains(result.matrixUser),
                        isAlreadyJoined = isJoined,
                        isAlreadyInvited = isInvited,
                        isUnresolved = result.isUnresolved,
                    )
                }.toImmutableList())
            }
        }.launchIn(this)
    }

    private suspend fun fetchMembers(
        room: JoinedRoom,
        roomMembers: MutableState<AsyncData<ImmutableList<RoomMember>>>
    ) {
        suspend {
            room.filterMembers("", coroutineDispatchers.io).toImmutableList()
        }.runCatchingUpdatingState(roomMembers)
    }
}
