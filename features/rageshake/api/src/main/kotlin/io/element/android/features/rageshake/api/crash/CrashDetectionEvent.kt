/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.crash

<<<<<<< HEAD:features/createroom/impl/src/main/kotlin/io/element/android/features/createroom/impl/configureroom/RoomVisibilityItem.kt
enum class RoomVisibilityItem {
    Private,

    // TCHAP - Enable PrivateNotEncrypted room
    PrivateNotEncrypted,

    Public,
    AskToJoin,
=======
sealed interface CrashDetectionEvent {
    data object ResetAllCrashData : CrashDetectionEvent
    data object ResetAppHasCrashed : CrashDetectionEvent
>>>>>>> main-element:features/rageshake/api/src/main/kotlin/io/element/android/features/rageshake/api/crash/CrashDetectionEvent.kt
}
