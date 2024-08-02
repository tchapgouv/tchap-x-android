/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class PinnedMessagesBannerPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val itemFactory: PinnedMessagesBannerItemFactory,
    private val featureFlagService: FeatureFlagService,
) : Presenter<PinnedMessagesBannerState> {

    @Composable
    override fun present(): PinnedMessagesBannerState {
        var pinnedItems by remember {
            mutableStateOf<List<PinnedMessagesBannerItem>>(emptyList())
        }

        fun onItemsChange(newItems: List<PinnedMessagesBannerItem>) {
            pinnedItems = newItems
        }

        var currentPinnedMessageIndex by rememberSaveable {
            mutableIntStateOf(0)
        }

        LaunchedEffect(pinnedItems) {
            val pinnedMessageCount = pinnedItems.size
            if (currentPinnedMessageIndex >= pinnedMessageCount) {
                currentPinnedMessageIndex = (pinnedMessageCount - 1).coerceAtLeast(0)
            }
        }

        PinnedMessagesBannerItemsEffect(::onItemsChange)

        fun handleEvent(event: PinnedMessagesBannerEvents) {
            when (event) {
                is PinnedMessagesBannerEvents.MoveToNextPinned -> {
                    if (currentPinnedMessageIndex < pinnedItems.size - 1) {
                        currentPinnedMessageIndex++
                    } else {
                        currentPinnedMessageIndex = 0
                    }
                }
            }
        }

        return PinnedMessagesBannerState(
            pinnedMessagesCount = pinnedItems.size,
            currentPinnedMessage = pinnedItems.getOrNull(currentPinnedMessageIndex),
            currentPinnedMessageIndex = currentPinnedMessageIndex,
            eventSink = ::handleEvent
        )
    }

    @OptIn(FlowPreview::class)
    @Composable
    private fun PinnedMessagesBannerItemsEffect(
        onItemsChange: (List<PinnedMessagesBannerItem>) -> Unit,
    ) {
        val isFeatureEnabled by featureFlagService.isFeatureEnabledFlow(FeatureFlags.PinnedEvents).collectAsState(initial = false)
        val updatedOnItemsChange by rememberUpdatedState(onItemsChange)

        LaunchedEffect(isFeatureEnabled) {
            if (!isFeatureEnabled) return@LaunchedEffect

            val pinnedEventsTimeline = room.pinnedEventsTimeline().getOrNull() ?: return@LaunchedEffect
            pinnedEventsTimeline.timelineItems
                .debounce(300.milliseconds)
                .map { timelineItems ->
                    timelineItems.mapNotNull { timelineItem ->
                        itemFactory.create(timelineItem)
                    }
                }
                .onEach { newItems ->
                    updatedOnItemsChange(newItems)
                }.onCompletion {
                    pinnedEventsTimeline.close()
                }
                .launchIn(this)
        }
    }
}
