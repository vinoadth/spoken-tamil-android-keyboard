package com.standardspokentamil.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TypingSessionTest {

    /** A text field with the cursor always at the end, like the start of a fresh message. */
    private class FakeEditor : TypingSession.Editor {
        val text = StringBuilder()
        var composing: IntRange? = null
        var finishCalls = 0
        var deleteCalls = 0

        override fun setComposingText(text: String) {
            val start = replaceComposing(text)
            composing = if (text.isEmpty()) null else start until this.text.length
        }

        override fun finishComposingText() {
            finishCalls++
            composing = null
        }

        override fun commitText(text: String) {
            replaceComposing(text)
            composing = null
        }

        override fun deleteBackward() {
            deleteCalls++
            if (text.isNotEmpty()) text.setLength(text.offsetByCodePoints(text.length, -1))
        }

        private fun replaceComposing(replacement: String): Int {
            val start = composing?.first ?: text.length
            text.replace(start, text.length, replacement)
            return start
        }

        val composingText: String? get() = composing?.let { text.substring(it.first, it.last + 1) }
    }

    private lateinit var editor: FakeEditor
    private lateinit var session: TypingSession
    private val queries = mutableListOf<String>()
    private var suggestions: List<String> = emptyList()

    @Before
    fun setUp() {
        editor = FakeEditor()
        session = TypingSession(
            editor,
            suggest = { queries += it; listOf("${it}1", "${it}2") },
            onSuggestions = { suggestions = it },
        )
        session.suggestionsEnabled = true
    }

    private fun type(vararg keys: String) = keys.forEach(session::type)

    @Test
    fun lettersAreComposedAndSuggested() {
        type("a", "v")
        assertEquals("av", editor.text.toString())
        assertEquals("av", editor.composingText)
        assertEquals("av", session.composingWord)
        assertEquals(listOf("a", "av"), queries)
        assertEquals(listOf("av1", "av2"), suggestions)
    }

    @Test
    fun shiftLevelLettersAreComposed() {
        type("\u0163", "a", "\u0146")
        assertEquals("\u0163a\u0146", editor.composingText)
    }

    @Test
    fun spaceEndsTheWordAndClearsSuggestions() {
        type("a", "v", " ")
        assertEquals("av ", editor.text.toString())
        assertNull(editor.composing)
        assertEquals(1, editor.finishCalls)
        assertEquals("", session.composingWord)
        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun punctuationAndDigitsEndTheWord() {
        type("a", ",", "b", "1", "c", ".")
        assertEquals("a,b1c.", editor.text.toString())
        assertNull(editor.composing)
        assertEquals(listOf("a", "b", "c"), queries)
    }

    @Test
    fun newWordStartsAfterASeparator() {
        type("a", " ", "b", "c")
        assertEquals("a bc", editor.text.toString())
        assertEquals("bc", editor.composingText)
    }

    @Test
    fun backspaceShortensTheComposingWord() {
        type("a", "b", "c")
        session.backspace()
        assertEquals("ab", editor.text.toString())
        assertEquals("ab", editor.composingText)
        assertEquals(listOf("ab1", "ab2"), suggestions)
        assertEquals(0, editor.deleteCalls)
    }

    @Test
    fun backspacingTheLastLetterEndsComposition() {
        type("a")
        session.backspace()
        assertEquals("", editor.text.toString())
        assertNull(editor.composing)
        assertEquals(1, editor.finishCalls)
        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun backspaceWithoutAWordDeletesFromTheField() {
        type("a", " ")
        session.backspace()
        assertEquals(1, editor.deleteCalls)
        assertEquals("a", editor.text.toString())
        assertEquals("", session.composingWord)
    }

    @Test
    fun backspaceRemovesAWholeCodePoint() {
        val supplementaryLetter = String(Character.toChars(0x1D51E))
        type("a", supplementaryLetter)
        session.backspace()
        assertEquals("a", editor.composingText)
    }

    @Test
    fun pickingASuggestionReplacesTheWordAndAddsASpace() {
        editor.text.append("hi ")
        type("a", "v")
        session.pickSuggestion("avan")
        assertEquals("hi avan ", editor.text.toString())
        assertNull(editor.composing)
        assertEquals("", session.composingWord)
        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun typingAfterAPickStartsANewWord() {
        type("a")
        session.pickSuggestion("avan")
        type("b")
        assertEquals("avan b", editor.text.toString())
        assertEquals("b", editor.composingText)
    }

    @Test
    fun selectionAtTheEndOfTheWordKeepsComposing() {
        type("a", "b")
        session.onSelectionChanged(2, 2, 0, 2)
        assertEquals("ab", session.composingWord)
        assertEquals(0, editor.finishCalls)
        assertEquals(listOf("ab1", "ab2"), suggestions)
    }

    @Test
    fun movingTheCursorAwayFinishesTheWord() {
        type("a", "b")
        session.onSelectionChanged(1, 1, 0, 2)
        assertEquals("", session.composingWord)
        assertEquals(1, editor.finishCalls)
        assertEquals("ab", editor.text.toString())
        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun selectingTextFinishesTheWord() {
        type("a", "b")
        session.onSelectionChanged(0, 2, 0, 2)
        assertEquals("", session.composingWord)
        assertEquals(1, editor.finishCalls)
    }

    @Test
    fun composingRemovedByTheAppForgetsTheWord() {
        type("a", "b")
        session.onSelectionChanged(0, 0, -1, -1)
        assertEquals("", session.composingWord)
        assertEquals(0, editor.finishCalls)
        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun selectionChangesWithoutAWordAreIgnored() {
        type("a", " ")
        suggestions = listOf("sentinel")
        session.onSelectionChanged(0, 0, -1, -1)
        assertEquals(listOf("sentinel"), suggestions)
        assertEquals(1, editor.finishCalls)
    }

    @Test
    fun finishWordKeepsTheTypedText() {
        type("a", "b")
        session.finishWord()
        assertEquals("ab", editor.text.toString())
        assertNull(editor.composing)
        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun finishWordWithoutAWordDoesNothing() {
        session.finishWord()
        assertEquals(0, editor.finishCalls)
    }

    @Test
    fun resetForgetsTheWordWithoutTouchingTheField() {
        type("a", "b")
        session.reset()
        assertEquals("", session.composingWord)
        assertEquals(0, editor.finishCalls)
        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun disabledSessionCommitsDirectlyAndNeverSuggests() {
        session.suggestionsEnabled = false
        type("a", "b")
        assertEquals("ab", editor.text.toString())
        assertNull(editor.composing)
        assertTrue(queries.isEmpty())
        assertEquals(emptyList<String>(), suggestions)
        session.backspace()
        assertEquals(1, editor.deleteCalls)
    }

    @Test
    fun disablingMidWordFinishesIt() {
        type("a", "b")
        session.suggestionsEnabled = false
        assertEquals("ab", editor.text.toString())
        assertNull(editor.composing)
        assertEquals(1, editor.finishCalls)
        assertEquals(emptyList<String>(), suggestions)
    }

    @Test
    fun reenablingDoesNotRepeatTheLastWord() {
        type("a")
        session.suggestionsEnabled = false
        session.suggestionsEnabled = true
        type("b")
        assertEquals("ab", editor.text.toString())
        assertEquals("b", editor.composingText)
    }
}
