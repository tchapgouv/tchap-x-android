/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.RageshakeConfig
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.sessionIdFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@ContributesBinding(AppScope::class)
@AssistedInject
class DefaultBugReporterUrlProvider(
    private val bugReportAppNameProvider: BugReportAppNameProvider,
    private val enterpriseService: EnterpriseService,
    // TCHAP : BUG_REPORT_URL depending of homeserver
    private val matrixClientProvider: MatrixClientProvider,
    private val sessionStore: SessionStore,
) : BugReporterUrlProvider {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun provide(): Flow<HttpUrl?> {
        if (bugReportAppNameProvider.provide().isEmpty()) return flowOf(null)
        return sessionStore.sessionIdFlow().flatMapLatest { sessionId ->
            val sessionIdAsUserId = sessionId?.let(::SessionId)
            enterpriseService.bugReportUrlFlow(sessionIdAsUserId)
                .map { bugReportUrl ->
                    when (bugReportUrl) {
                        is BugReportUrl.Custom -> bugReportUrl.url
                        BugReportUrl.Disabled -> null
                        // TCHAP : BUG_REPORT_URL depending of homeserver
//                        BugReportUrl.UseDefault -> RageshakeConfig.BUG_REPORT_URL.takeIf { it.isNotEmpty() }
                        BugReportUrl.UseDefault -> {
                            val serverName = sessionIdAsUserId?.let { matrixClientProvider.getOrNull(it)?.userIdServerName() }
                                ?: sessionIdAsUserId?.domainName
                                ?: ""

                            if (serverName.isNotEmpty()) {
                                HttpUrl.Builder()
                                    .scheme("https")
                                    .host("matrix.$serverName")
                                    .addPathSegments(RageshakeConfig.TCHAP_BUG_REPORT_PATH)
                                    .build()
                                    .toString()
                            } else {
                                RageshakeConfig.BUG_REPORT_URL.takeIf { it.isNotEmpty() }
                            }
                        }
                    }
                }
                .map { it?.toHttpUrl() }
        }
    }
}
