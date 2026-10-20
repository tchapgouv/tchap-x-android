/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import fr.gouv.tchap.android.features.enterprise.api.HomeserverConfiguration
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeTemporaryMatrixClient
import io.element.android.libraries.matrix.test.FakeTemporaryMatrixClientFactory
import io.element.android.libraries.matrix.test.accountprovider.anAccountProviderGeneric
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test

class DefaultEnterpriseServiceTest {
    private val homeserverList = HomeserverConfiguration(
        defaultHomeserverList = listOf(A_HOMESERVER_URL)
    )

    @Test
<<<<<<< HEAD
    fun homeserverWhitelist() {
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
        assertThat(defaultEnterpriseService.homeserverAllowList()).isEmpty()
    }

    @Test
    fun `isAllowedToConnectToHomeserver is true for all homeserver urls`() = runTest {
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
        assertThat(defaultEnterpriseService.isAllowedToConnectToHomeserver(A_HOMESERVER_URL)).isTrue()
=======
    fun accountProviderAllowList() = runTest {
        val defaultEnterpriseService = createDefaultEnterpriseService()
        assertThat(defaultEnterpriseService.accountProviderAllowList()).isEmpty()
    }

    @Test
    fun `canConnectToAnyAccountProvider is always true`() = runTest {
        val defaultEnterpriseService = createDefaultEnterpriseService()
        assertThat(defaultEnterpriseService.canConnectToAnyAccountProvider()).isTrue()
    }

    @Test
    fun `isAllowedToConnectToAccountProvider is true for all account providers`() = runTest {
        val defaultEnterpriseService = createDefaultEnterpriseService()
        assertThat(defaultEnterpriseService.isAllowedToConnectToAccountProvider(anAccountProviderGeneric())).isTrue()
>>>>>>> main-element
    }

    @Test
    fun `isEnterpriseUser always return false`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        assertThat(defaultEnterpriseService.isEnterpriseUser(A_SESSION_ID)).isFalse()
    }

    @Test
    fun `semanticColorsFlow always emits the same value`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        defaultEnterpriseService.semanticColorsFlow(null).test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(SemanticColorsLightDark.default)
            awaitComplete()
        }
    }

    @Test
    fun `brandColorsFlow always emits null`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        defaultEnterpriseService.brandColorsFlow(null).test {
            val initialState = awaitItem()
            assertThat(initialState).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `semanticColorsFlow always emits the same value for a session`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        defaultEnterpriseService.semanticColorsFlow(A_SESSION_ID).test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(SemanticColorsLightDark.default)
            awaitComplete()
        }
    }

    @Test
    fun `overrideBrandColor has no effect`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        defaultEnterpriseService.overrideBrandColor(A_SESSION_ID, "aColor")
    }

    @Test
    fun `firebasePushGateway returns null`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        assertThat(defaultEnterpriseService.firebasePushGateway()).isNull()
    }

    @Test
    fun `unifiedPushDefaultPushGateway returns null`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        assertThat(defaultEnterpriseService.unifiedPushDefaultPushGateway()).isNull()
    }

    @Test
    fun `bugReportUrlFlow only emits UseDefault`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        defaultEnterpriseService.bugReportUrlFlow(A_SESSION_ID).test {
            assertThat(awaitItem()).isEqualTo(BugReportUrl.UseDefault)
            awaitComplete()
        }
    }

    @Test
    fun `getNoisyNotificationChannelId returns null`() = runTest {
<<<<<<< HEAD
        val defaultEnterpriseService = DefaultEnterpriseService(homeserverList)
=======
        val defaultEnterpriseService = createDefaultEnterpriseService()
>>>>>>> main-element
        assertThat(defaultEnterpriseService.getNoisyNotificationChannelId(A_SESSION_ID)).isNull()
    }

    @Test
    fun `isElementProEnforced checks using a temporary client`() = runTest {
        val closeLambda = lambdaRecorder<Unit> {}
        val getUrlLambda = lambdaRecorder<String, Result<ByteArray>> {
            Result.success("""{"enforce_element_pro": true}""".toByteArray())
        }
        val client = FakeTemporaryMatrixClient(
            getUrlResult = getUrlLambda,
            closeLambda = closeLambda,
        )
        val defaultEnterpriseService = createDefaultEnterpriseService(client = client)
        assertThat(defaultEnterpriseService.isElementProEnforced(A_HOMESERVER_URL)).isTrue()

        // Verify that the temporary client was used to fetch the URL and then closed
        val expectedUrl = "${A_HOMESERVER_URL.ensureProtocol()}/.well-known/element/element.json"
        getUrlLambda.assertions().isCalledOnce().with(value(expectedUrl))
        closeLambda.assertions().isCalledOnce()
    }

    private fun TestScope.createDefaultEnterpriseService(
        client: FakeTemporaryMatrixClient = FakeTemporaryMatrixClient(),
        jsonProvider: JsonProvider = { Json }
    ) = DefaultEnterpriseService(
        temporaryMatrixClientFactory = FakeTemporaryMatrixClientFactory(createResult = { Result.success(client) }),
        jsonProvider = jsonProvider,
        dispatchers = testCoroutineDispatchers(),
    )
}
