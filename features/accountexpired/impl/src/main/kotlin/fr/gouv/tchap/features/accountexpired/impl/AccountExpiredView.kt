/*
 * MIT License
 *
 * Copyright (c) 2026. DINUM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.gouv.tchap.features.accountexpired.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.OnBoardingPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AccountExpiredView(
    state: AccountExpiredState,
    modifier: Modifier = Modifier,
    onClickLearnMore: () -> Unit,
) {
    OnBoardingPage(
        modifier = modifier,
        renderBackground = false,
        content = {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BigIcon(
                    style = BigIcon.Style.Default(
                        vectorIcon = CompoundIcons.Calendar(),
                        useCriticalTint = true,
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.tchap_screen_account_expired_title),
                    textAlign = TextAlign.Center,
                    style = ElementTheme.typography.fontHeadingMdBold,
                    color = ElementTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.tchap_screen_account_expired_description),
                    textAlign = TextAlign.Center,
                    style = ElementTheme.typography.fontBodyLgRegular,
                    color = ElementTheme.colors.textSecondary,
                )
                Spacer(modifier = Modifier.height(24.dp))
                CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.textSecondary) {
                    TextButton(
                        text = stringResource(CommonStrings.action_learn_more),
                        onClick = onClickLearnMore,
                    )
                }
            }
        },
        footer = {
            ButtonColumnMolecule {
                Button(
                    text = stringResource(CommonStrings.action_continue),
                    showProgress = state.isLoadingRestartSync,
                    enabled = !state.isLoadingSendEmail,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { state.eventSink(AccountExpiredEvents.Retry) },
                )
                TextButton(
                    text = stringResource(R.string.tchap_screen_account_expired_button_send_email),
                    showProgress = state.isLoadingSendEmail,
                    enabled = !state.isLoadingRestartSync,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { state.eventSink(AccountExpiredEvents.SendEmail) },
                )
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun AccountExpiredViewPreview() = ElementPreview {
    AccountExpiredView(
        state = AccountExpiredState(
            eventSink = {},
            isLoadingRestartSync = false,
            isLoadingSendEmail = false,
        ),
        onClickLearnMore = {},
    )
}
