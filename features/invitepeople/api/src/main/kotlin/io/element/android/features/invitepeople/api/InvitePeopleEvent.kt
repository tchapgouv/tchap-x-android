/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.api

<<<<<<< HEAD:features/invitepeople/api/src/main/kotlin/io/element/android/features/invitepeople/api/InvitePeopleEvents.kt
interface InvitePeopleEvents {
    data object SendInvites : InvitePeopleEvents
    data object CloseSearch : InvitePeopleEvents
    data object CheckExternalsAndSendInvites : InvitePeopleEvents // TCHAP external user
    data object ClearError : InvitePeopleEvents
=======
/**
 * Events the invite people UI sends to its presenter through [InvitePeopleState.eventSink].
 */
interface InvitePeopleEvent {
    data object SendInvites : InvitePeopleEvent
    data object CloseSearch : InvitePeopleEvent
    data object ClearError : InvitePeopleEvent
>>>>>>> main-element:features/invitepeople/api/src/main/kotlin/io/element/android/features/invitepeople/api/InvitePeopleEvent.kt
}
