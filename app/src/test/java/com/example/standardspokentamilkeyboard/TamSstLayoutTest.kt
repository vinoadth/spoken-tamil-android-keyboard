package com.example.standardspokentamilkeyboard

import com.example.standardspokentamilkeyboard.layout.TamSstLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TamSstLayoutTest {

    @Test
    fun matchesX11Values() {
        val keys = TamSstLayout.letterKeys.associateBy { it.id }
        fun levels(id: String) = keys.getValue(id).let { listOf(it.base, it.shift, it.alt, it.altShift) }

        assertEquals(listOf("\u01F9", "\u0146", "q", "Q"), levels("AD01"))
        assertEquals(listOf("s", "\u015F", "\u015B", "S"), levels("AC02"))
        assertEquals(listOf("\u00E0", "\u00E6", "x", "X"), levels("AB02"))
        assertEquals(listOf("l", "\u013C", "l", "L"), levels("AC09"))
    }

    @Test
    fun rowsHaveExpectedSizes() {
        assertEquals(10, TamSstLayout.numberRow.size)
        assertEquals(10, TamSstLayout.topRow.size)
        assertEquals(9, TamSstLayout.homeRow.size)
        assertEquals(7, TamSstLayout.bottomRow.size)
        assertEquals(TamSstLayout.topRow + TamSstLayout.homeRow + TamSstLayout.bottomRow, TamSstLayout.letterKeys)
    }

    @Test
    fun keyIdsAreUnique() {
        val all = TamSstLayout.numberRow + TamSstLayout.letterKeys + TamSstLayout.punctuation
        assertEquals(all.size, all.map { it.id }.toSet().size)
    }

    @Test
    fun letterKeysHaveFourLevelsWithAsciiOnLevelsThreeAndFour() {
        for (key in TamSstLayout.letterKeys) {
            val alt = requireNotNull(key.alt) { key.id }
            val altShift = requireNotNull(key.altShift) { key.id }
            assertTrue(key.id, alt.single() in 'a'..'z' || alt == "\u015B")
            assertTrue(key.id, altShift.single() in 'A'..'Z')
        }
    }

    @Test
    fun everyLevelIsASingleCharacter() {
        val all = TamSstLayout.numberRow + TamSstLayout.letterKeys + TamSstLayout.punctuation
        for (key in all) {
            for (level in listOfNotNull(key.base, key.shift, key.alt, key.altShift)) {
                assertEquals("${key.id} '$level'", 1, level.codePointCount(0, level.length))
            }
        }
    }

    @Test
    fun nonLetterKeysHaveTwoLevels() {
        for (key in TamSstLayout.numberRow + TamSstLayout.punctuation) {
            assertNull(key.id, key.alt)
            assertNull(key.id, key.altShift)
        }
    }
}
