/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.annotations.ContributesNode
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class ConfigureRoomNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ConfigureRoomPresenter.Factory,
    private val analyticsService: AnalyticsService,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onCreateRoomSuccess(roomId: RoomId)
    }

    @Parcelize
    data class Inputs(
        val isSpace: Boolean,
        val parentSpaceId: RoomId?,
    ) : NodeInputs, Parcelable

    private val inputs = inputs<Inputs>()

    private val presenter = presenterFactory.create(inputs.isSpace, inputs.parentSpaceId)

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.CreateRoom))
            }
        )
    }

    private val callback: Callback = callback()

    // TCHAP : Add header Learn More link in space creation
    private fun onClickLearnMore(activity: Activity, darkTheme: Boolean) {
        activity.openUrlInChromeCustomTab(null, darkTheme, LearnMoreConfig.HOW_TO_CREATE_SPACE)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        val state = presenter.present()
        ConfigureRoomView(
            state = state,
            modifier = modifier,
            onBackClick = this::navigateUp,
            // TCHAP : Add header Learn More link in space creation
            onClickLearnMore = { onClickLearnMore(activity, isDark) },
            onCreateRoomSuccess = callback::onCreateRoomSuccess,
        )
    }
}
