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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.anAccountProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData

open class LoginHintStateProvider : PreviewParameterProvider<LoginHintState> {
    override val values: Sequence<LoginHintState>
        get() = sequenceOf(
            aLoginHintState(),
            // Loading
            aLoginHintState(loginMode = AsyncData.Loading()),
            // Error
            aLoginHintState(loginMode = AsyncData.Failure(Exception("An error occurred"))),
        )
}

fun aLoginHintState(
    applicationName: String = "Tchap",
    accountProvider: AccountProvider = anAccountProvider(),
    formState: LoginFormState = LoginFormState.Default,
    isAccountCreation: Boolean = false,
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    eventSink: (LoginHintEvents) -> Unit = {},
) = LoginHintState(
    applicationName = applicationName,
    accountProvider = accountProvider,
    isAccountCreation = isAccountCreation,
    formState = formState,
    loginMode = loginMode,
    eventSink = eventSink,
)

fun aLoginFormState(
    login: String = "",
) = LoginFormState(
    login = login,
)
