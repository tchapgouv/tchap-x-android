/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.account.PreferencesAccountCallback
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.emoji.api.picker.EmojiPickerRenderer
<<<<<<< HEAD
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.user.MatrixUser
=======
import io.element.android.libraries.matrix.api.core.SessionId
>>>>>>> main-element

@ContributesNode(SessionScope::class)
@AssistedInject
class PreferencesRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val featureFlagService: FeatureFlagService,
    private val presenter: PreferencesRootPresenter,
    private val emojiPickerRenderer: EmojiPickerRenderer,
) : Node(buildContext, plugins = plugins) {
    interface Callback : PreferencesAccountCallback, Plugin {
        fun navigateToAddAccount()
        fun navigateToSession(sessionId: SessionId)
        fun navigateToAnalyticsSettings()
        fun navigateToAbout()
        fun onOpenFAQ(activity: Activity, darkTheme: Boolean)
        fun navigateToDeveloperSettings()
        fun navigateToLockScreenSettings()
        fun navigateToMediaSettings()
        fun navigateToLocationSettings()
        fun navigateToLabs()
    }

    private val callback: Callback = callback()

    private fun onManageAccountClick(
        activity: Activity,
        url: String?,
        isDark: Boolean,
    ) {
        url?.let {
            activity.openUrlInChromeCustomTab(
                null,
                darkTheme = isDark,
                url = it
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()

<<<<<<< HEAD
        val showMatrixId by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.ShowMatrixId)
        }.collectAsState(false)

=======
>>>>>>> main-element
        PreferencesRootView(
            state = state,
            emojiPickerRenderer = emojiPickerRenderer,
            modifier = modifier,
            onBackClick = this::navigateUp,
            onAddAccountClick = callback::navigateToAddAccount,
            onSwitchToSession = callback::navigateToSession,
            onOpenAnalytics = callback::navigateToAnalyticsSettings,
            onOpenAbout = callback::navigateToAbout,
<<<<<<< HEAD
            onOpenFAQ = { callback.onOpenFAQ(activity, isDark) },
            onSecureBackupClick = callback::navigateToSecureBackup,
=======
>>>>>>> main-element
            onOpenDeveloperSettings = callback::navigateToDeveloperSettings,
            onOpenMediaSettings = callback::navigateToMediaSettings,
            onOpenLocationSettings = callback::navigateToLocationSettings,
            onOpenLabs = callback::navigateToLabs,
            onOpenLockScreenSettings = callback::navigateToLockScreenSettings,
            onOpenRageShake = callback::navigateToBugReport,
            onSecureBackupClick = callback::navigateToSecureBackup,
            onModerationAndSafetyClick = callback::navigateToModerationAndSafety,
            onLinkNewDeviceClick = callback::navigateToLinkNewDevice,
            onManageAccountClick = { onManageAccountClick(activity, it, isDark) },
            onOpenNotificationSettings = callback::navigateToNotificationSettings,
<<<<<<< HEAD
            onOpenLockScreenSettings = callback::navigateToLockScreenSettings,
            onOpenUserProfile = callback::navigateToUserProfile,
            onOpenBlockedUsers = callback::navigateToBlockedUsers,
            onSignOutClick = {
                if (state.directLogoutState.canDoDirectSignOut) {
                    state.directLogoutState.eventSink(DirectLogoutEvent.Logout(ignoreSdkError = false))
                } else {
                    callback.startSignOutFlow()
                }
            },
            onDeactivateClick = callback::startAccountDeactivationFlow,
            showMatrixId = showMatrixId,
=======
            onEditProfileClick = callback::navigateToUserProfile,
            onSignOutClick = callback::startSignOutFlow,
            onDeactivateClick = callback::startAccountDeactivationFlow
>>>>>>> main-element
        )
    }
}
