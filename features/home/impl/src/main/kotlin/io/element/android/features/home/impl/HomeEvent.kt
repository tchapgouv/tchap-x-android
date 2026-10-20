/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

sealed interface HomeEvent {
<<<<<<< HEAD
    // TCHAP : Space default action is now conversation filtering (add automatic Reselect Last filter)
//    data class SelectHomeNavigationBarItem(val item: HomeNavigationBarItem) : HomeEvent
    data class SelectHomeNavigationBarItem(val item: HomeNavigationBarItem, val reselectLastFilters: Boolean = true) : HomeEvent
    data class SwitchToAccount(val sessionId: SessionId) : HomeEvent
=======
    data class SelectHomeNavigationBarItem(val item: HomeNavigationBarItem) : HomeEvent
    data object DismissMultiAccountAnnouncement : HomeEvent
    data object DismissSwipeAvatarAnnouncement : HomeEvent
>>>>>>> main-element
}
