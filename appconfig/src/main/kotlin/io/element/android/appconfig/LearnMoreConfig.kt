/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

object LearnMoreConfig {
    const val ENCRYPTION_URL: String = "https://element.io/help#encryption"

    // :tchap: Change DEVICE_VERIFICATION_URL & SECURE_BACKUP_URL URLS
    const val DEVICE_VERIFICATION_URL: String = "https://aide.tchap.numerique.gouv.fr/fr/article/comment-verifier-un-nouvel-appareil-sur-tchap-xm0b0y/"
    const val SECURE_BACKUP_URL: String = "https://aide.tchap.numerique.gouv.fr/fr/article/quest-ce-que-la-sauvegarde-automatique-des-messages-1sdg43v/"
    // :tchap: end

    const val IDENTITY_CHANGE_URL: String = "https://element.io/help#encryption18"

    // :tchap: Change FAQ_URL, HOW_TO_CREATE_SPACE & HOW_TO_RENEW_MY_ACCOUNT URLS
    const val FAQ_URL: String = "https://aide.tchap.numerique.gouv.fr"
    const val HOW_TO_CREATE_SPACE: String = "https://aide.tchap.numerique.gouv.fr/fr/article/comment-creer-un-espace-sur-tchap-web-1wmlenx/"
    const val HOW_TO_RENEW_MY_ACCOUNT = "https://aide.tchap.numerique.gouv.fr/fr/article/renouvellement-de-votre-compte-tchap-expiration-1g4e6xh/"
    // :tchap: end

    const val HISTORY_VISIBLE_URL: String = "https://element.io/en/help#e2ee-history-sharing"

    // :tchap: Add backup guide to connect from Tchap Classique
    const val BACKUP_GUIDE_TCHAP_CLASSIQUE: String = "https://aide.tchap.numerique.gouv.fr/fr/article/verifier-la-sauvegarde-automatique-pour-la-connexion-depuis-tchap-classique-6zsiqk/"
    // :tchap: end
}
