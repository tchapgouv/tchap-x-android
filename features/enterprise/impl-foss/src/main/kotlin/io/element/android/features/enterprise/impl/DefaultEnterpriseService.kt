/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import androidx.compose.ui.graphics.Color
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import fr.gouv.tchap.android.features.enterprise.api.HomeserverConfiguration
import io.element.android.compound.tokens.generated.compoundColorsDark
import io.element.android.compound.tokens.generated.compoundColorsLight
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.random.Random

@ContributesBinding(AppScope::class)
class DefaultEnterpriseService(
    private val homeserverConfiguration: HomeserverConfiguration
) : EnterpriseService {
    override val isEnterpriseBuild = false
    override var selectedHomeserver: Int = -1

    override suspend fun isEnterpriseUser(sessionId: SessionId) = false

    override fun defaultHomeserverList(): List<String> = homeserverConfiguration.defaultHomeserverList

    override fun getNextRandomHomeserver(): String {
        val homeservers = homeserverConfiguration.defaultHomeserverList

        selectedHomeserver = when (selectedHomeserver) {
            -1 -> Random.nextInt(homeservers.size)
            else -> (selectedHomeserver + 1) % homeservers.size
        }

        return homeservers[selectedHomeserver]
    }

    override suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String) = true

    override suspend fun overrideBrandColor(sessionId: SessionId?, brandColor: String?) = Unit

    override fun brandColorsFlow(sessionId: SessionId?): Flow<Color?> {
        return flowOf(null)
    }

    override fun semanticColorsFlow(sessionId: SessionId?): Flow<SemanticColorsLightDark> {
        return flowOf(SemanticColorsLightDark.default)
    }

    override fun firebasePushGateway(): String? = null
    override fun unifiedPushDefaultPushGateway(): String? = null

    override fun bugReportUrlFlow(sessionId: SessionId?): Flow<BugReportUrl> {
        return flowOf(BugReportUrl.UseDefault)
    }
}
