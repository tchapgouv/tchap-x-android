/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.atomic.molecules.MatrixBadgeRowMolecule
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun BadgeList(
    roomBadge: ImmutableList<RoomBadge>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (roomBadge.isNotEmpty()) {
            MatrixBadgeRowMolecule(
                data = roomBadge.map {
                    it.toMatrixBadgeData()
                }.toImmutableList(),
            )
        }
    }
}
