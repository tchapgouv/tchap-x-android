/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package config

object BuildTimeConfig {
    const val APPLICATION_ID = "fr.gouv.tchap.android.x"
    const val APPLICATION_NAME = "Tchap X"
    const val GOOGLE_APP_ID_RELEASE = "1:912726360885:android:d097de99a4c23d2700427c" // Tchap TODO : Fix ID
    const val GOOGLE_APP_ID_DEBUG = "1:912726360885:android:def0a4e454042e9b00427c" // Tchap TODO : Fix ID
    const val GOOGLE_APP_ID_NIGHTLY = "1:912726360885:android:e17435e0beb0303000427c" // Tchap TODO : Fix ID
    const val URL_ACCEPTABLE_USE = "https://tchap.numerique.gouv.fr/cgu"
    const val URL_PRIVACY = "https://tchap.numerique.gouv.fr/politique-de-confidentialite"
    const val SERVICES_MAPTILER_BASE_URL = "https://openmaptiles.geo.data.gouv.fr/styles"
    const val SERVICES_MAPTILER_LIGHT_MAPID = "osm-bright"
    const val SERVICES_MAPTILER_DARK_MAPID = "dark-matter"

    val METADATA_HOST_REVERSED: String? = null
    val URL_WEBSITE: String? = null
    val URL_LOGO: String? = null
    val URL_COPYRIGHT: String? = null
    val URL_POLICY: String? = null
    val SERVICES_MAPTILER_APIKEY: String? = null
    val SERVICES_POSTHOG_HOST: String? = null
    val SERVICES_POSTHOG_APIKEY: String? = null
    val SERVICES_SENTRY_DSN: String? = null
    val BUG_REPORT_URL: String? = null
    val BUG_REPORT_APP_NAME: String? = null

    const val PUSH_CONFIG_INCLUDE_FIREBASE = true
    const val PUSH_CONFIG_INCLUDE_UNIFIED_PUSH = true
}
