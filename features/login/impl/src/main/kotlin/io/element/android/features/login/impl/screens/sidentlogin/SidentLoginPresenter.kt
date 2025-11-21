/*
 * MIT License
 *
 * Copyright (c) 2025. DINUM
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

package io.element.android.features.login.impl.screens.sidentlogin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import dev.zacsweers.metro.Inject
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta

@Inject
class SidentLoginPresenter(
    private val buildMeta: BuildMeta,
    private val loginHelper: LoginHelper,
    private val accountProviderDataSource: AccountProviderDataSource,
) : Presenter<SidentLoginState> {
    @Composable
    override fun present(): SidentLoginState {
        val localCoroutineScope = rememberCoroutineScope()

        val formState = rememberSaveable {
            mutableStateOf(LoginFormState.Default)
        }
        val accountProvider by accountProviderDataSource.flow.collectAsState()

        val loginMode by loginHelper.collectLoginMode()

        fun handleEvents(event: SidentLoginEvents) {
            when (event) {
                is SidentLoginEvents.SetLogin -> updateFormState(formState) {
                    copy(login = event.login)
                }
                is SidentLoginEvents.OnContinue -> loginHelper.getHomeserverFromLoginHint(
                    coroutineScope = localCoroutineScope,
                    accountProviderDataSource = accountProviderDataSource,
                    loginHint = formState.value.login,
                )
                SidentLoginEvents.ClearError -> loginHelper.clearError()
            }
        }

        return SidentLoginState(
            applicationName = buildMeta.applicationName,
            accountProvider = accountProvider,
            formState = formState.value,
            eventSink = ::handleEvents,
            loginMode = loginMode,
        )
    }

    private fun updateFormState(formState: MutableState<LoginFormState>, updateLambda: LoginFormState.() -> LoginFormState) {
        formState.value = updateLambda(formState.value)
    }
}
