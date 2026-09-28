package com.example.standardspokentamilkeyboard.dictionary

import com.example.standardspokentamilkeyboard.layout.TamSstLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordNormalizerTest {

    @Test
    fun canonicalComposesDecomposedCharacters() {
        assertEquals("\u0163", WordNormalizer.canonical("t\u0327"))
        assertEquals("\u0163a", WordNormalizer.canonical("\u0163a"))
    }

    @Test
    fun keyFoldsShiftLevelToUnshiftedCharacter() {
        assertEquals("ta\u01F9i", WordNormalizer.key("\u0163a\u0146i"))
    }

    @Test
    fun keyFoldsAsciiLongPressLevelsToUnshiftedCharacter() {
        assertEquals("\u01F9", WordNormalizer.key("q"))
        assertEquals("\u01F9", WordNormalizer.key("Q"))
        assertEquals("\u00F9", WordNormalizer.key("w"))
        assertEquals("s", WordNormalizer.key("\u015B"))
        assertEquals("s", WordNormalizer.key("S"))
    }

    @Test
    fun keyFoldsDecomposedInputLikePrecomposed() {
        assertEquals(WordNormalizer.key("\u0163"), WordNormalizer.key("t\u0327"))
    }

    @Test
    fun keyLowercasesCharactersNotOnTheKeyboard() {
        assertEquals("\u00FC", WordNormalizer.key("\u00DC"))
        assertEquals("1", WordNormalizer.key("1"))
    }

    @Test
    fun everyCharacterOfALetterKeyFoldsToThatKeysBase() {
        for (key in TamSstLayout.letterKeys) {
            for (level in listOfNotNull(key.base, key.shift, key.alt, key.altShift)) {
                assertEquals("${key.id} level '$level'", key.base, WordNormalizer.key(level))
            }
        }
    }

    @Test
    fun noCharacterAppearsOnTwoDifferentLetterKeys() {
        val owners = HashMap<String, String>()
        for (key in TamSstLayout.letterKeys) {
            for (level in listOfNotNull(key.base, key.shift, key.alt, key.altShift).distinct()) {
                val previous = owners.put(level, key.id)
                assertTrue("'$level' is on both $previous and ${key.id}", previous == null || previous == key.id)
            }
        }
    }

    @Test
    fun isWordTextAcceptsLettersAndCombiningMarks() {
        assertTrue(WordNormalizer.isWordText("a"))
        assertTrue(WordNormalizer.isWordText("\u01F9"))
        assertTrue(WordNormalizer.isWordText("t\u0327"))
        assertTrue(WordNormalizer.isWordText("தமிழ்"))
    }

    @Test
    fun isWordTextRejectsEverythingElse() {
        for (text in listOf("", " ", "1", ".", ",", "-", "'", "a b", "a1", "\n")) {
            assertFalse("'$text'", WordNormalizer.isWordText(text))
        }
    }
}
