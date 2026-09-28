package com.example.standardspokentamilkeyboard.ime

import com.example.standardspokentamilkeyboard.dictionary.WordNormalizer

/**
 * Tracks the word being typed as composing text and publishes suggestions for it.
 * Kept free of Android types so the editing rules can be unit-tested against a fake [Editor].
 */
class TypingSession(
    private val editor: Editor,
    private val suggest: (String) -> List<String>,
    private val onSuggestions: (List<String>) -> Unit,
) {

    interface Editor {
        fun setComposingText(text: String)
        fun finishComposingText()
        fun commitText(text: String)

        /** Deletes backwards when no word is being composed (selection, previous character, …). */
        fun deleteBackward()
    }

    private val word = StringBuilder()

    val composingWord: String get() = word.toString()

    /** When false, text is committed immediately and no suggestions are offered. */
    var suggestionsEnabled: Boolean = false
        set(value) {
            if (field == value) return
            endWord()
            field = value
            publish()
        }

    fun type(text: String) {
        if (suggestionsEnabled && WordNormalizer.isWordText(text)) {
            word.append(text)
            editor.setComposingText(composingWord)
        } else {
            endWord()
            editor.commitText(text)
        }
        publish()
    }

    fun backspace() {
        if (word.isEmpty()) {
            editor.deleteBackward()
            return
        }
        word.setLength(word.offsetByCodePoints(word.length, -1))
        editor.setComposingText(composingWord)
        if (word.isEmpty()) editor.finishComposingText()
        publish()
    }

    fun pickSuggestion(suggestion: String) {
        word.clear()
        editor.commitText("$suggestion ")
        publish()
    }

    /** Mirrors `InputMethodService.onUpdateSelection`; ends the word if the cursor left it. */
    fun onSelectionChanged(selStart: Int, selEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        if (word.isEmpty()) return
        when {
            candidatesStart < 0 || candidatesEnd < 0 -> word.clear()
            selStart != selEnd || selEnd != candidatesEnd -> endWord()
            else -> return
        }
        publish()
    }

    /** Commits the word in progress as typed, e.g. when leaving the field. */
    fun finishWord() {
        endWord()
        publish()
    }

    private fun endWord() {
        if (word.isEmpty()) return
        word.clear()
        editor.finishComposingText()
    }

    /** Forgets the word without touching the editor, for a newly started field. */
    fun reset() {
        word.clear()
        publish()
    }

    private fun publish() {
        onSuggestions(if (suggestionsEnabled && word.isNotEmpty()) suggest(composingWord) else emptyList())
    }
}
