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

package fr.gouv.tchap.android.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.gouv.tchap.android.features.enterprise.api.HomeserverConfiguration
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.di.identifiers.SentryDsn
import io.element.android.libraries.di.identifiers.SentrySdkDsn
import io.element.android.x.R

@BindingContainer
@ContributesTo(AppScope::class)
object HomeserverModule {
    @Provides
    fun provideHomeserverConfiguration(
        @ApplicationContext context: Context,
    ): HomeserverConfiguration {
        val homeserverList = context.resources.getStringArray(R.array.default_homeservers).toList()
        return HomeserverConfiguration(
            defaultHomeserverList = homeserverList
        )
    }

    // :tchap: Tchap custom Sentry DSN
    fun getCustomTchapSentryURL(
        context: Context,
        homeserverConfiguration: HomeserverConfiguration
    ): String? {
        val homeserver = homeserverConfiguration.defaultHomeserverList.firstOrNull()
        val sentryKey = context.getString(R.string.sentry_key)
        return if (homeserver != null && sentryKey.isNotBlank()) {
            "https://$sentryKey@matrix.$homeserver/sentry/1"
        } else {
            null
        }
    }

    @Provides
    fun provideSentryDsn(
        @ApplicationContext context: Context,
        homeserverConfiguration: HomeserverConfiguration
    ): SentryDsn? {
        return getCustomTchapSentryURL(context, homeserverConfiguration)?.let { SentryDsn(it) }
    }

    // :tchap: Tchap custom Sentry DSN for the Matrix Rust SDK
    @Provides
    fun provideSentrySdkDsn(
        @ApplicationContext context: Context,
        homeserverConfiguration: HomeserverConfiguration
    ): SentrySdkDsn? {
        return getCustomTchapSentryURL(context, homeserverConfiguration)?.let { SentrySdkDsn(it) }
    }
    // :tchap: end
}
