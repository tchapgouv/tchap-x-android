/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.scanner

import io.element.android.libraries.matrix.api.UrlContentFetcher

/**
 * Provides the URL of the content scanner service for a given homeserver, if any is set up.
 */
fun interface ContentScannerUrlProvider {
    // :tchap: Provides homeserverURL used directly as the content scanner URL
//    /**
//     * Returns the URL of the content scanner service for the given [sessionId], or `null` if no content scanner is set up.
//     */
//    suspend fun getContentScannerUrl(sessionId: SessionId): Result<String?>

    /**
     * Returns the URL of the content scanner service from the homerserverURL.
     */
    suspend fun getContentScannerUrl(homeserverURL: String): Result<String?>
    // :tchap: end

    fun interface Factory {
        fun create(urlContentFetcher: UrlContentFetcher): ContentScannerUrlProvider
    }
}
