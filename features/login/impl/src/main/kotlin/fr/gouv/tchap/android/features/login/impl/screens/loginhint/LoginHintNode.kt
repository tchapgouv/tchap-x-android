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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.login.impl.util.openLearnMorePage
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.matrix.api.auth.OidcDetails

@ContributesNode(AppScope::class)
@AssistedInject
class LoginHintNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: LoginHintPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val isAccountCreation: Boolean,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(
        LoginHintPresenter.Params(
            isAccountCreation = inputs.isAccountCreation,
        )
    )

    interface Callback : Plugin {
        fun onLoginHintNeeded()
        fun onLoginPasswordNeeded()
        fun onOidcDetails(oidcDetails: OidcDetails)
        fun onCreateAccountContinue(url: String)
    }

    private fun onOidcDetails(data: OidcDetails) {
        plugins<Callback>().forEach { it.onOidcDetails(data) }
    }

    private fun onLoginHintNeeded() {
        plugins<Callback>().forEach { it.onLoginHintNeeded() }
    }

    private fun onLoginPasswordNeeded() {
        plugins<Callback>().forEach { it.onLoginPasswordNeeded() }
    }

    private fun onCreateAccountContinue(url: String) {
        plugins<Callback>().forEach { it.onCreateAccountContinue(url) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        LoginHintView(
            state = state,
            modifier = modifier,
            onBackClick = ::navigateUp,
            onOidcDetails = ::onOidcDetails,
            onNeedLoginHint = ::onLoginHintNeeded,
            onNeedLoginPassword = ::onLoginPasswordNeeded,
            onLearnMoreClick = { openLearnMorePage(context) },
            onCreateAccountContinue = ::onCreateAccountContinue,
        )
    }
}
