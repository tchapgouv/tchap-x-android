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

package fr.gouv.tchap.android.features.login.impl.screens.sidentlogin

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.features.login.impl.screens.onboarding.createLoginHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SidentLoginPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createSidentLoginPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.accountProvider.url).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
            assertThat(initialState.formState).isEqualTo(LoginFormState.Default)
            assertThat(initialState.loginMode).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.submitEnabled).isFalse()
        }
    }

    @Test
    fun `present - enter login`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        createSidentLoginPresenter(
            loginHelper = createLoginHelper(
                authenticationService = authenticationService,
            ),
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SidentLoginEvents.SetLogin(A_USER_NAME))
            val loginState = awaitItem()
            assertThat(loginState.formState).isEqualTo(LoginFormState(login = A_USER_NAME))
            assertThat(loginState.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - submit`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        createSidentLoginPresenter(
            loginHelper = createLoginHelper(
                authenticationService = authenticationService,
            ),
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SidentLoginEvents.SetLogin(A_USER_NAME))
            skipItems(1)
            val loginState = awaitItem()
            loginState.eventSink.invoke(SidentLoginEvents.OnContinue)
            val submitState = awaitItem()
            assertThat(submitState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginMode).isEqualTo(AsyncData.Success(A_SESSION_ID))
        }
    }

    @Test
    fun `present - submit with error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        createSidentLoginPresenter(
            loginHelper = createLoginHelper(
                authenticationService = authenticationService,
            ),
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SidentLoginEvents.SetLogin(A_USER_NAME))
            skipItems(1)
            val loginState = awaitItem()
            authenticationService.givenLoginError(AN_EXCEPTION)
            loginState.eventSink.invoke(SidentLoginEvents.OnContinue)
            val submitState = awaitItem()
            assertThat(submitState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginMode).isEqualTo(AsyncData.Failure<SessionId>(AN_EXCEPTION))
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        createSidentLoginPresenter(
            loginHelper = createLoginHelper(
                authenticationService = authenticationService,
            ),
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SidentLoginEvents.SetLogin(A_USER_NAME))
            skipItems(1)
            val loginState = awaitItem()
            authenticationService.givenLoginError(AN_EXCEPTION)
            loginState.eventSink.invoke(SidentLoginEvents.OnContinue)
            val submitState = awaitItem()
            assertThat(submitState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            // Check an error was returned
            assertThat(loggedInState.loginMode).isEqualTo(AsyncData.Failure<SessionId>(AN_EXCEPTION))
            // Assert the error is then cleared
            loggedInState.eventSink(SidentLoginEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    private fun createSidentLoginPresenter(
        loginHelper: LoginHelper = createLoginHelper(),
        accountProviderDataSource: AccountProviderDataSource = AccountProviderDataSource(FakeEnterpriseService()),
    ): SidentLoginPresenter = SidentLoginPresenter(
        loginHelper = loginHelper,
        params = SidentLoginPresenter.Params(isAccountCreation = true),
        accountProviderDataSource = accountProviderDataSource,
        buildMeta = aBuildMeta(),
    )
}
