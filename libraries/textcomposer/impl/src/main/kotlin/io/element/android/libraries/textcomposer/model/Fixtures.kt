/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import io.element.android.wysiwyg.compose.RichTextEditorState

fun aTextEditorStateMarkdown(
    initialText: String? = "",
    initialFocus: Boolean = false,
    isRoomEncrypted: Boolean? = null,
    // :tchap: Warning on file upload when room is not encrypted
    isRoomJoinRulePublic: Boolean? = null,
    // :tchap: end
): TextEditorState {
    return TextEditorState.Markdown(
        aMarkdownTextEditorState(
            initialText = initialText,
            initialFocus = initialFocus,
        ),
        isRoomEncrypted = isRoomEncrypted,
        // :tchap: Warning on file upload when room is not encrypted
        isRoomJoinRulePublic = isRoomJoinRulePublic,
        // :tchap: end
    )
}

fun aMarkdownTextEditorState(
    initialText: String? = "",
    initialFocus: Boolean = false,
): MarkdownTextEditorState {
    return MarkdownTextEditorState(
        initialText = initialText,
        initialFocus = initialFocus,
    )
}

fun aTextEditorStateRich(
    initialText: String = "",
    initialHtml: String = initialText,
    initialMarkdown: String = initialText,
    initialFocus: Boolean = false,
    isRoomEncrypted: Boolean? = null,
    // :tchap: Warning on file upload when room is not encrypted
    isRoomJoinRulePublic: Boolean? = null,
    // :tchap: end
): TextEditorState {
    return TextEditorState.Rich(
        aRichTextEditorState(
            initialText = initialText,
            initialHtml = initialHtml,
            initialMarkdown = initialMarkdown,
            initialFocus = initialFocus,
        ),
        isRoomEncrypted = isRoomEncrypted,
        // :tchap: Warning on file upload when room is not encrypted
        isRoomJoinRulePublic = isRoomJoinRulePublic,
        // :tchap: end
    )
}

fun aRichTextEditorState(
    initialText: String = "",
    initialHtml: String = initialText,
    initialMarkdown: String = initialText,
    initialFocus: Boolean = false,
): RichTextEditorState {
    return RichTextEditorState(
        initialHtml = initialHtml,
        initialMarkdown = initialMarkdown,
        initialFocus = initialFocus,
    )
}
