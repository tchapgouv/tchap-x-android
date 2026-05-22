/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.loginwithclassic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.BitmapAvatar
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.auth.OAuthDetails
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginWithClassicView(
    state: LoginWithClassicState,
    onOtherOptionsClick: () -> Unit,
    onOAuthDetails: (OAuthDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onNeedLoginHint: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by remember(state.loginMode) {
        derivedStateOf {
            state.loginMode is AsyncData.Loading
        }
    }

    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        // :tchap: tchap-legacy-connection
//        background = { OnboardingBackground() },
        background = {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                painter = painterResource(id = io.element.android.libraries.designsystem.R.drawable.onboarding_legacy_bg),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        },
        // :tchap: end
        isScrollable = true,
        header = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .size(54.dp),
                        // :tchap: tchap-legacy-connection
//                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(15.dp))
//                        .background(ElementTheme.colors.bgCanvasDefault, shape = RoundedCornerShape(15.dp)),
                        // :tchap: end
                    contentAlignment = Alignment.Center,
                ) {
                    val resId = if (state.isElementPro) {
                        R.drawable.element_pro_logo
                    } else {
                        // :tchap: tchap-legacy-connection
//                        R.drawable.element_foss_logo
                        R.drawable.tchap_logo
                        // :tchap: end
                    }
                    Image(
                        // :tchap: tchap-legacy-connection
//                        modifier = Modifier.size(37.5.dp),
                        modifier = Modifier.size(54.dp),
                        // :tchap: end
                        painter = painterResource(id = resId),
                        contentDescription = null,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.screen_onboarding_welcome_title),
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontHeadingMdBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(40.dp))
                BitmapAvatar(
                    avatarData = AvatarData(
                        id = state.userId.value,
                        name = state.displayName,
                        // Not used here
                        url = null,
                        size = AvatarSize.UserHeader,
                    ),
                    bitmap = state.avatar,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    text = stringResource(R.string.screen_onboarding_welcome_back),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                // User display name
                if (state.displayName != null) {
                    Text(
                        text = state.displayName,
                        style = ElementTheme.typography.fontHeadingLgBold,
                        color = ElementTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                }
                // UserId
                // :tchap: tchap-legacy-connection
                if (state.showMatrixId) {
                // :tchap: end
                    Text(
                        text = state.userId.value,
                        style = if (state.displayName == null) ElementTheme.typography.fontHeadingLgBold else ElementTheme.typography.fontBodyLgRegular,
                        color = ElementTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
                // Min spacing
                Spacer(Modifier.height(45.dp))
                ButtonColumnMolecule {
                    Button(
                        // :tchap: tchap-legacy-connection
//                        text = stringResource(CommonStrings.action_continue),
                        text = stringResource(R.string.tchap_screen_onboarding_legacy_continue),
                        // :tchap: end
                        showProgress = isLoading,
                        onClick = {
                            state.eventSink(LoginWithClassicEvent.Submit)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.loginContinue)
                    )
                    OutlinedButton(
                        // :tchap: tchap-legacy-connection
//                        text = stringResource(CommonStrings.common_other_options),
                        text = stringResource(R.string.tchap_screen_onboarding_legacy_other_account),
                        // :tchap: end
                        onClick = onOtherOptionsClick,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.loginContinue)
                    )
                }
            }
        },
        footer = {},
    )

    AsyncActionView(
        async = state.loginWithClassicAction,
        onErrorDismiss = {
            state.eventSink(LoginWithClassicEvent.ClearError)
        },
        onSuccess = {
            // noop, the view will be closed
        },
        progressDialog = {
            // The button is showing the progress
        }
    )
    LoginModeView(
        loginMode = state.loginMode,
        onClearError = {
            state.eventSink(LoginWithClassicEvent.ClearError)
        },
        onLearnMoreClick = onLearnMoreClick,
        onOAuthDetails = onOAuthDetails,
        onNeedLoginPassword = onNeedLoginPassword,
        onNeedLoginHint = onNeedLoginHint,
        onCreateAccountContinue = onCreateAccountContinue,
    )
}

@PreviewsDayNight
@Composable
internal fun LoginWithClassicViewPreview(@PreviewParameter(LoginWithClassicStateProvider::class) state: LoginWithClassicState) = ElementPreview {
    LoginWithClassicView(
        state = state,
        onOtherOptionsClick = {},
        onOAuthDetails = {},
        onNeedLoginPassword = {},
        onNeedLoginHint = {},
        onLearnMoreClick = {},
        onCreateAccountContinue = {},
    )
}
