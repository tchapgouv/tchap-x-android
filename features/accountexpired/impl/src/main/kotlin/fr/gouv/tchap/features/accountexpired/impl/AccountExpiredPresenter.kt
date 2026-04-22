/*
 * MIT License
 *
 * Copyright (c) 2026. DINUM
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

package fr.gouv.tchap.features.accountexpired.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AssistedInject
class AccountExpiredPresenter(
    @Assisted private val sessionId: SessionId,
    private val matrixClientProvider: MatrixClientProvider,
) : Presenter<AccountExpiredState> {
    @AssistedFactory
    interface Factory {
        fun create(sessionId: SessionId): AccountExpiredPresenter
    }

    private val matrixClient by lazy { matrixClientProvider.getOrNull(sessionId)!! }

    @Composable
    override fun present(): AccountExpiredState {
        val localCoroutineScope = rememberCoroutineScope()
        var isLoadingRestartSync by remember { mutableStateOf(false) }
        var isLoadingSendEmail by remember { mutableStateOf(false) }

        fun handleEvents(event: AccountExpiredEvents) {
            when (event) {
                AccountExpiredEvents.Retry -> {
                    Timber.d("Retry clicked for session $sessionId")
                    isLoadingRestartSync = true
                    localCoroutineScope.launch {
                        matrixClient.syncService.startSync()
                        delay(2500)
                        isLoadingRestartSync = false
                    }
                }
                AccountExpiredEvents.SendEmail -> {
                    Timber.d("Send email clicked for session $sessionId")
                    isLoadingSendEmail = true
                    localCoroutineScope.launch {
                        matrixClient.accountExpirationSendEmail()
                        isLoadingSendEmail = false
                    }
                }
            }
        }

        return AccountExpiredState(
            isLoadingRestartSync = isLoadingRestartSync,
            isLoadingSendEmail = isLoadingSendEmail,
            eventSink = ::handleEvents
        )
    }
}
