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

package fr.gouv.tchap.android.features.login.impl.screens.loginhint

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta

@AssistedInject
class LoginHintPresenter(
    @Assisted private val params: Params,
    private val buildMeta: BuildMeta,
    private val loginHelper: LoginHelper,
    private val accountProviderDataSource: AccountProviderDataSource,
) : Presenter<LoginHintState> {
    data class Params(
        val isAccountCreation: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(params: Params): LoginHintPresenter
    }

    @Composable
    override fun present(): LoginHintState {
        val localCoroutineScope = rememberCoroutineScope()

        val formState = rememberSaveable {
            mutableStateOf(LoginFormState.Default)
        }
        val accountProvider by accountProviderDataSource.flow.collectAsState()

        val loginMode by loginHelper.collectLoginMode()

        fun handleEvents(event: LoginHintEvents) {
            when (event) {
                is LoginHintEvents.SetLogin -> updateFormState(formState) {
                    copy(login = event.login)
                }
                is LoginHintEvents.OnContinue -> loginHelper.getHomeserverFromLoginHint(
                    coroutineScope = localCoroutineScope,
                    isAccountCreation = params.isAccountCreation,
                    accountProviderDataSource = accountProviderDataSource,
                    loginHint = formState.value.login,
                )
                LoginHintEvents.ClearError -> loginHelper.clearError()
            }
        }

        return LoginHintState(
            applicationName = buildMeta.applicationName,
            accountProvider = accountProvider,
            isAccountCreation = params.isAccountCreation,
            formState = formState.value,
            eventSink = ::handleEvents,
            loginMode = loginMode,
        )
    }

    private fun updateFormState(formState: MutableState<LoginFormState>, updateLambda: LoginFormState.() -> LoginFormState) {
        formState.value = updateLambda(formState.value)
    }
}
