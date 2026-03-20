/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerEvent
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerStateProvider
import io.element.android.features.messages.api.timeline.voicemessages.composer.aVoiceMessageComposerState
import io.element.android.libraries.designsystem.components.dialogs.AlertDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.textcomposer.TextComposer
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
internal fun MessageComposerView(
    state: MessageComposerState,
    voiceMessageState: VoiceMessageComposerState,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    var showAprilFoolPopup by remember { mutableIntStateOf(0) }

    fun sendMessage() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val isAprilFool = now.year == 2026 && now.month == Month.APRIL && now.day == 1
        if (isAprilFool && !state.isAprilFoolShown) {
            showAprilFoolPopup = 1
        } else {
            state.eventSink(MessageComposerEvent.SendMessage)
        }
    }

    if (showAprilFoolPopup == 1) {
        AlertDialog(
            title = "Mise à jour de sécurité !",
            content = "Vos messages sont désormais acheminés par notre nouvelle flotte de pigeons voyageurs.\n" +
                "Le protocole P2P (Pigeon to Pigeon) reste chiffré de bout en bout via la traduction de vos messages en hiéroglyphes égyptiens !\n\n" +
                "Temps de livraison estimé de votre message : 3 à 5 jours ouvrés.",
            onDismiss = {
                showAprilFoolPopup = 2
            },
        )
    }
    if (showAprilFoolPopup == 2) {
        AlertDialog(
            title = "Pigeon d'Avril !!!",
            content = "Bon début de mois d'Avril à tous•tes !",
            onDismiss = {
                showAprilFoolPopup = 0
                state.eventSink(MessageComposerEvent.MarkAprilFoolAsShown)
                state.eventSink(MessageComposerEvent.SendMessage)
            },
        )
    }

    fun sendUri(uri: Uri) {
        state.eventSink(MessageComposerEvent.SendUri(uri))
    }

    fun onAddAttachment() {
        state.eventSink(MessageComposerEvent.AddAttachment)
    }

    fun onCloseSpecialMode() {
        state.eventSink(MessageComposerEvent.CloseSpecialMode)
    }

    fun onDismissTextFormatting() {
        view.clearFocus()
        state.eventSink(MessageComposerEvent.ToggleTextFormatting(enabled = false))
    }

    fun onSuggestionReceived(suggestion: Suggestion?) {
        state.eventSink(MessageComposerEvent.SuggestionReceived(suggestion))
    }

    fun onError(error: Throwable) {
        state.eventSink(MessageComposerEvent.Error(error))
    }

    fun onTyping(typing: Boolean) {
        state.eventSink(MessageComposerEvent.TypingNotice(typing))
    }

    val coroutineScope = rememberCoroutineScope()
    fun onRequestFocus() {
        coroutineScope.launch {
            state.textEditorState.requestFocus()
        }
    }

    val onVoiceRecorderEvent = { press: VoiceMessageRecorderEvent ->
        voiceMessageState.eventSink(VoiceMessageComposerEvent.RecorderEvent(press))
    }

    val onSendVoiceMessage = {
        voiceMessageState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
    }

    val onDeleteVoiceMessage = {
        voiceMessageState.eventSink(VoiceMessageComposerEvent.DeleteVoiceMessage)
    }

    val onVoicePlayerEvent = { event: VoiceMessagePlayerEvent ->
        voiceMessageState.eventSink(VoiceMessageComposerEvent.PlayerEvent(event))
    }

    TextComposer(
        modifier = modifier,
        state = state.textEditorState,
        voiceMessageState = voiceMessageState.voiceMessageState,
        onRequestFocus = ::onRequestFocus,
        onSendMessage = ::sendMessage,
        composerMode = state.mode,
        showTextFormatting = state.showTextFormatting,
        onResetComposerMode = ::onCloseSpecialMode,
        onAddAttachment = ::onAddAttachment,
        onDismissTextFormatting = ::onDismissTextFormatting,
        onVoiceRecorderEvent = onVoiceRecorderEvent,
        onVoicePlayerEvent = onVoicePlayerEvent,
        onSendVoiceMessage = onSendVoiceMessage,
        onDeleteVoiceMessage = onDeleteVoiceMessage,
        onReceiveSuggestion = ::onSuggestionReceived,
        resolveMentionDisplay = state.resolveMentionDisplay,
        resolveAtRoomMentionDisplay = state.resolveAtRoomMentionDisplay,
        onError = ::onError,
        onTyping = ::onTyping,
        onSelectRichContent = ::sendUri,
    )
}

@PreviewsDayNight
@Composable
internal fun MessageComposerViewPreview(
    @PreviewParameter(MessageComposerStateProvider::class) state: MessageComposerState,
) = ElementPreview {
    Column {
        MessageComposerView(
            modifier = Modifier.height(IntrinsicSize.Min),
            state = state,
            voiceMessageState = aVoiceMessageComposerState(),
        )
        MessageComposerView(
            modifier = Modifier.height(200.dp),
            state = state,
            voiceMessageState = aVoiceMessageComposerState(),
        )
        DisabledComposerView()
    }
}

@PreviewsDayNight
@Composable
internal fun MessageComposerViewVoicePreview(
    @PreviewParameter(VoiceMessageComposerStateProvider::class) state: VoiceMessageComposerState,
) = ElementPreview {
    Column {
        MessageComposerView(
            modifier = Modifier.height(IntrinsicSize.Min),
            state = aMessageComposerState(),
            voiceMessageState = state,
        )
    }
}
