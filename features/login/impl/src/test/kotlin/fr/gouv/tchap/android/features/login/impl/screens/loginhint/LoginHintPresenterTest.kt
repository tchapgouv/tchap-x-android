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

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.accountprovider.anAccountProviderDataSource
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.features.login.impl.screens.onboarding.createLoginModePresenter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.auth.aMatrixHomeServerDetails
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LoginHintPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createLoginHintPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.accountProvider.url).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
            assertThat(initialState.formState).isEqualTo(LoginFormState.Default)
            assertThat(initialState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.submitEnabled).isFalse()
        }
    }

    @Test
    fun `present - enter login`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        createLoginHintPresenter(
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginHintEvents.SetLogin(A_USER_NAME))
            val loginState = awaitItem()
            assertThat(loginState.formState).isEqualTo(LoginFormState(login = A_USER_NAME))
            assertThat(loginState.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - submit`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            }
        )
        val enterpriseService = FakeEnterpriseService(
            defaultHomeserverListResult = { listOf("matrix.org") },
            selectedHomeserver = 0
        )
        createLoginHintPresenter(
            enterpriseService = enterpriseService,
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginHintEvents.SetLogin(A_USER_NAME))
            val loginState = awaitItem()
            loginState.eventSink.invoke(LoginHintEvents.OnContinue)
            val submitState = awaitItem()
            assertThat(submitState.loginModeState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.loginModeState.loginMode).isEqualTo(AsyncData.Success(LoginMode.PasswordLogin))
            assertThat(successState.accountProvider.url).isEqualTo("https://matrix.org")
        }
    }

    @Test
    fun `present - submit with email discovery`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            }
        )
        val enterpriseService = FakeEnterpriseService(
            defaultHomeserverListResult = { listOf("agent.dinum.tchap.gouv.fr") },
            selectedHomeserver = 0
        )
        createLoginHintPresenter(
            enterpriseService = enterpriseService,
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginHintEvents.SetLogin("user@dinum.tchap.gouv.fr"))
            val loginState = awaitItem()
            loginState.eventSink.invoke(LoginHintEvents.OnContinue)
            val submitState = awaitItem()
            assertThat(submitState.loginModeState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.accountProvider.url).isEqualTo("https://agent.dinum.tchap.gouv.fr")
        }
    }

    @Test
    fun `present - submit with error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            }
        )
        val enterpriseService = FakeEnterpriseService(
            defaultHomeserverListResult = { listOf("matrix.org") },
            selectedHomeserver = 0
        )
        createLoginHintPresenter(
            enterpriseService = enterpriseService,
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginHintEvents.SetLogin(A_USER_NAME))
            val loginState = awaitItem()
            loginState.eventSink.invoke(LoginHintEvents.OnContinue)
            val submitState = awaitItem()
            assertThat(submitState.loginModeState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.loginModeState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val enterpriseService = FakeEnterpriseService(
            defaultHomeserverListResult = { emptyList() },
            selectedHomeserver = 0
        )
        createLoginHintPresenter(
            enterpriseService = enterpriseService,
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginHintEvents.SetLogin(A_USER_NAME))
            val loginState = awaitItem()
            loginState.eventSink.invoke(LoginHintEvents.OnContinue)
            val submitState = awaitItem()
            assertThat(submitState.loginModeState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.loginModeState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
            errorState.eventSink(LoginHintEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    private fun createLoginHintPresenter(
        enterpriseService: EnterpriseService = FakeEnterpriseService(
            defaultHomeserverListResult = { listOf("matrix.org") },
            selectedHomeserver = 0
        ),
        authenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
        accountProviderDataSource: AccountProviderDataSource = anAccountProviderDataSource(enterpriseService = enterpriseService),
    ): LoginHintPresenter = LoginHintPresenter(
        params = LoginHintPresenter.Params(isAccountCreation = true),
        accountProviderDataSource = accountProviderDataSource,
        buildMeta = aBuildMeta(),
        enterpriseService = enterpriseService,
        authenticationService = authenticationService,
        loginModePresenter = createLoginModePresenter(authenticationService = authenticationService),
    )
}
