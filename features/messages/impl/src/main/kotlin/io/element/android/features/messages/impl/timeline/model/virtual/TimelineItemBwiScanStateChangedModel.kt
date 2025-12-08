/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.virtual

import de.bwi.messenger.libraries.matrix.api.BwiContentScannerScanState

data class TimelineItemBwiScanStateChangedModel(
    val eventId: String,
    val newScanState: BwiContentScannerScanState,
) : TimelineItemVirtualModel {
    override val type: String = "TimelineItemBwiScanStateChangedModel"
}
