/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.virtual

import de.bwi.messenger.libraries.matrix.api.BwiContentScannerScanState

data class TimelineItemBwiScanStateChangedModel(
    val eventId: String,
    val newScanState: BwiContentScannerScanState,
) : TimelineItemVirtualModel {
    override val type: String = "TimelineItemBwiScanStateChangedModel"
}
