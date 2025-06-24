/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.loginpassword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginPasswordPresenter @Inject constructor(
    private val authenticationService: MatrixAuthenticationService,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val defaultLoginUserStory: DefaultLoginUserStory,
) : Presenter<LoginPasswordState> {
    @Composable
    override fun present(): LoginPasswordState {
        val localCoroutineScope = rememberCoroutineScope()
        val loginAction: MutableState<AsyncData<SessionId>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        val formState = rememberSaveable {
            mutableStateOf(LoginFormState.Default)
        }
        val accountProvider by accountProviderDataSource.flow.collectAsState()

        fun handleEvents(event: LoginPasswordEvents) {
            when (event) {
                is LoginPasswordEvents.SetLogin -> updateFormState(formState) {
                    copy(login = event.login)
                }
                is LoginPasswordEvents.SetPassword -> updateFormState(formState) {
                    copy(password = event.password)
                }
                LoginPasswordEvents.Submit -> {
                    localCoroutineScope.submit(formState.value, loginAction)
                }
                LoginPasswordEvents.ClearError -> loginAction.value = AsyncData.Uninitialized
            }
        }

        return LoginPasswordState(
            accountProvider = accountProvider,
            formState = formState.value,
            loginAction = loginAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.submit(formState: LoginFormState, loggedInState: MutableState<AsyncData<SessionId>>) = launch {
        loggedInState.value = AsyncData.Loading()

        // Tchap: [beta DINUM] - intercept login here in formState.login.
        val matrixId = tchapConvertEmailToMatrixId(formState.login)

        authenticationService.login(matrixId.trim(), formState.password)
            .onSuccess { sessionId ->
                // We will not navigate to the WaitList screen, so the login user story is done
                defaultLoginUserStory.setLoginFlowIsDone(true)
                loggedInState.value = AsyncData.Success(sessionId)
            }
            .onFailure { failure ->
                loggedInState.value = AsyncData.Failure(failure)
            }
    }

    private fun updateFormState(formState: MutableState<LoginFormState>, updateLambda: LoginFormState.() -> LoginFormState) {
        formState.value = updateLambda(formState.value)
    }

    // Tchap: [beta DINUM] - convert matrixID to email if necessary
    private fun tchapConvertEmailToMatrixId(identifier: String): String {
        // If the user email doesn't contain an '@' character, it can be the start of a matrixID (e.g. 'firstname.lastname-myDomain').
        // Try to replace last hyphen ('-') by an '@' to make it looks like a email address.

        // Don't touch the identifier if it is not an email address.
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            return identifier
        }

        // Find last '@' in identifier.
        val lastIndexOfArobase = identifier.lastIndexOf('@')

        if (lastIndexOfArobase < 0) {
            return identifier
        }

        // Split identifier around last '@'.
        val prefix = identifier.substring(0, lastIndexOfArobase)
        val suffix = identifier.substring(lastIndexOfArobase + 1, identifier.length)

        // Build MatrixID joining prefix and suffix with a '-' sign.
        val matrixId = "${prefix}-${suffix}"

        return matrixId
    }
}
