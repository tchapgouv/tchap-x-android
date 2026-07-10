/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package config

object BuildTimeConfig {
    const val APPLICATION_ID = "fr.gouv.tchap.android.x"
    const val APPLICATION_NAME = "Tchap"
//    const val GOOGLE_APP_ID_RELEASE = "1:912726360885:android:d097de99a4c23d2700427c"
//    const val GOOGLE_APP_ID_DEBUG = "1:912726360885:android:def0a4e454042e9b00427c"
//    const val GOOGLE_APP_ID_NIGHTLY = "1:912726360885:android:e17435e0beb0303000427c"

    const val URL_ACCEPTABLE_USE = "https://tchap.numerique.gouv.fr/cgu"
    const val URL_PRIVACY = "https://tchap.numerique.gouv.fr/politique-de-confidentialite"
    const val SERVICES_MAPTILER_BASE_URL = "https://openmaptiles.geo.data.gouv.fr/styles"
    const val SERVICES_MAPTILER_LIGHT_MAPID = "osm-bright"
    const val SERVICES_MAPTILER_DARK_MAPID = "fiord-color"

    val METADATA_HOST_REVERSED: String? = null
    val URL_WEBSITE: String? = null

    // TCHAP : specific URL
    const val URL_LOGO: String = "https://www.tchap.gouv.fr/vector-icons/300.png"

    val URL_COPYRIGHT: String? = null

    // TCHAP : specific URL
    const val URL_POLICY = "https://tchap.numerique.gouv.fr/politique-de-confidentialite"

    val SERVICES_MAPTILER_APIKEY: String? = null
    val SERVICES_POSTHOG_HOST: String? = null
    val SERVICES_POSTHOG_APIKEY: String? = null
    val SERVICES_SENTRY_DSN: String? = null
    val SERVICES_SENTRY_DSN_RUST: String? = null
    val BUG_REPORT_URL: String? = null

    // TCHAP : specific Bug report App Name
    const val BUG_REPORT_APP_NAME = "tchap-x-android"

    const val PUSH_CONFIG_INCLUDE_FIREBASE = true
    const val PUSH_CONFIG_INCLUDE_UNIFIED_PUSH = true

    // :tchap: Push config for Gateway URL
    const val PUSH_CONFIG_GATEWAY_URL_PROD = "https://sygnal.tchap.gouv.fr/_matrix/push/v1/notify"
    const val PUSH_CONFIG_GATEWAY_URL_PREPROD = "https://sygnal.preprod.tchap.gouv.fr/_matrix/push/v1/notify"
    const val PUSH_CONFIG_GATEWAY_URL_DEV = "https://sygnal.tchap.incubateur.net/_matrix/push/v1/notify"
    // :tchap: end

    // :tchap: Push config for App variants
    const val GOOGLE_APP_ID_PROD = "1:1092909174787:android:4c71128ded60ae8d2a7e36"
    const val GOOGLE_APP_ID_PROD_NIGHTLY = "1:1092909174787:android:ee3b65c51d5df33a2a7e36"
    const val GOOGLE_APP_ID_PROD_DEBUG = "1:1092909174787:android:5e519186f69f05822a7e36"
    const val GOOGLE_APP_ID_PREPROD = "1:1092909174787:android:0183db2fee705f4c2a7e36"
    const val GOOGLE_APP_ID_PREPROD_NIGHTLY = "1:1092909174787:android:9ecebfc24cdd11952a7e36"
    const val GOOGLE_APP_ID_PREPROD_DEBUG = "1:1092909174787:android:5dea9c3d42482c132a7e36"
    const val GOOGLE_APP_ID_DEV = "1:1092909174787:android:4f1445fd40d73ebf2a7e36"
    const val GOOGLE_APP_ID_DEV_NIGHTLY = "1:1092909174787:android:190ed65bcb2965002a7e36"
    const val GOOGLE_APP_ID_DEV_DEBUG = "1:1092909174787:android:ab600c14603565a62a7e36"
    // :tchap: end
}
