package com.example.standardspokentamilkeyboard.dictionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryTest {

    private fun dictionary(vararg entries: Pair<String, Int>) =
        Dictionary(entries.map { (word, frequency) -> Dictionary.Entry(word, frequency) })

    @Test
    fun emptyDictionarySuggestsNothing() {
        assertTrue(Dictionary.EMPTY.isEmpty)
        assertEquals(0, Dictionary.EMPTY.size)
        assertEquals(emptyList<String>(), Dictionary.EMPTY.suggest("a"))
    }

    @Test
    fun sizeCountsEntries() {
        val dict = dictionary("aa" to 1, "ab" to 1)
        assertFalse(dict.isEmpty)
        assertEquals(2, dict.size)
    }

    @Test
    fun suggestsWordsStartingWithPrefixByFrequency() {
        val dict = dictionary("aval" to 5, "avan" to 9, "ange" to 3, "idu" to 100)
        assertEquals(listOf("avan", "aval", "ange"), dict.suggest("a"))
        assertEquals(listOf("avan", "aval"), dict.suggest("av"))
    }

    @Test
    fun emptyOrUnmatchedInputSuggestsNothing() {
        val dict = dictionary("avan" to 1)
        assertEquals(emptyList<String>(), dict.suggest(""))
        assertEquals(emptyList<String>(), dict.suggest("x"))
        assertEquals(emptyList<String>(), dict.suggest("avanga"))
    }

    @Test
    fun respectsLimit() {
        val dict = dictionary("aa" to 4, "ab" to 3, "ac" to 2, "ad" to 1)
        assertEquals(listOf("aa", "ab", "ac"), dict.suggest("a"))
        assertEquals(listOf("aa"), dict.suggest("a", limit = 1))
        assertEquals(emptyList<String>(), dict.suggest("a", limit = 0))
        assertEquals(4, dict.suggest("a", limit = 10).size)
    }

    @Test
    fun findsWordsTypedWithoutShiftOrLongPress() {
        val dict = dictionary("\u0163a\u0146i" to 1)
        assertEquals(listOf("\u0163a\u0146i"), dict.suggest("ta\u01F9"))
        assertEquals(listOf("\u0163a\u0146i"), dict.suggest("\u0163a"))
    }

    @Test
    fun matchesDecomposedInput() {
        val dict = dictionary("\u0163a" to 1)
        assertEquals(listOf("\u0163a"), dict.suggest("t\u0327"))
    }

    @Test
    fun exactWordRanksFirstRegardlessOfFrequency() {
        val dict = dictionary("avan" to 1, "avanga" to 100)
        assertEquals(listOf("avan", "avanga"), dict.suggest("avan"))
    }

    @Test
    fun wordsMatchingTheTypedLettersExactlyRankAboveShiftVariants() {
        val dict = dictionary("tani" to 1, "\u0163ani" to 100)
        assertEquals(listOf("\u0163ani", "tani"), dict.suggest("\u0163a"))
        assertEquals(listOf("tani", "\u0163ani"), dict.suggest("ta"))
    }

    @Test
    fun equalFrequencyPrefersShorterThenAlphabetical() {
        val dict = dictionary("abcd" to 1, "abd" to 1, "abc" to 1)
        assertEquals(listOf("abc", "abd", "abcd"), dict.suggest("ab"))
    }

    @Test
    fun keysSortedAroundThePrefixDoNotLeakIn() {
        val dict = dictionary("aa" to 1, "ab" to 1, "abc" to 1, "ac" to 1, "b" to 1)
        assertEquals(listOf("ab", "abc"), dict.suggest("ab"))
    }
}
