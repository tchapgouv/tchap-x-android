/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.media.AvatarAction

<<<<<<< HEAD:features/createroom/impl/src/main/kotlin/io/element/android/features/createroom/impl/configureroom/ConfigureRoomEvents.kt
sealed interface ConfigureRoomEvents {
    data class RoomNameChanged(val name: String) : ConfigureRoomEvents
    data class TopicChanged(val topic: String) : ConfigureRoomEvents
    data class JoinRuleChanged(val joinRuleItem: JoinRuleItem) : ConfigureRoomEvents
    data class RoomAddressChanged(val roomAddress: String) : ConfigureRoomEvents
    data object CreateRoom : ConfigureRoomEvents
    data class HandleAvatarAction(val action: AvatarAction) : ConfigureRoomEvents
    data class SetParentSpace(val space: SpaceRoom?) : ConfigureRoomEvents
    data object CancelCreateRoom : ConfigureRoomEvents

    // TCHAP : Add toggle to enable/disable public room federation
    data class PublicRoomLimitedToFederation(val isPublicRoomLimited: Boolean) : ConfigureRoomEvents
=======
sealed interface ConfigureRoomEvent {
    data class RoomNameChanged(val name: String) : ConfigureRoomEvent
    data class TopicChanged(val topic: String) : ConfigureRoomEvent
    data class JoinRuleChanged(val joinRuleItem: JoinRuleItem) : ConfigureRoomEvent
    data class RoomAddressChanged(val roomAddress: String) : ConfigureRoomEvent
    data object CreateRoom : ConfigureRoomEvent
    data class HandleAvatarAction(val action: AvatarAction) : ConfigureRoomEvent
    data class SetParentSpace(val space: SpaceRoom?) : ConfigureRoomEvent
    data object CancelCreateRoom : ConfigureRoomEvent
>>>>>>> main-element:features/createroom/impl/src/main/kotlin/io/element/android/features/createroom/impl/configureroom/ConfigureRoomEvent.kt
}
