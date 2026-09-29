package com.standardspokentamil.dictionary

import com.standardspokentamil.dictionary.DictionaryParser.Problem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryParserTest {

    private fun parse(vararg lines: String) = DictionaryParser.parse(lines.asSequence())

    @Test
    fun parsesWordsWithAndWithoutFrequency() {
        val result = parse("naan\t50", "enna")
        assertEquals(
            listOf(Dictionary.Entry("naan", 50), Dictionary.Entry("enna", DictionaryParser.DEFAULT_FREQUENCY)),
            result.entries,
        )
        assertTrue(result.problems.isEmpty())
    }

    @Test
    fun acceptsSpacesAsSeparatorAndSurroundingWhitespace() {
        val result = parse("  naan   50  ", "enna \t 7")
        assertEquals(listOf(Dictionary.Entry("naan", 50), Dictionary.Entry("enna", 7)), result.entries)
    }

    @Test
    fun skipsCommentsBlankLinesAndByteOrderMark() {
        val result = parse("\uFEFF# header", "", "   ", "# naan 5", "enna")
        assertEquals(listOf(Dictionary.Entry("enna", 1)), result.entries)
        assertTrue(result.problems.isEmpty())
    }

    @Test
    fun acceptsZeroFrequency() {
        assertEquals(listOf(Dictionary.Entry("naan", 0)), parse("naan 0").entries)
    }

    @Test
    fun storesWordsInComposedForm() {
        assertEquals("\u0163a", parse("t\u0327a").entries.single().word)
    }

    @Test
    fun reportsInvalidFrequency() {
        val result = parse("naan -1", "enna often", "idu 1.5")
        assertTrue(result.entries.isEmpty())
        assertEquals(listOf(1, 2, 3), result.problems.map { it.line })
    }

    @Test
    fun reportsExtraColumns() {
        val result = parse("naan 5 extra")
        assertTrue(result.entries.isEmpty())
        assertEquals(1, result.problems.single().line)
    }

    @Test
    fun reportsNonLetterWords() {
        val result = parse("naan1", "en-na", "i.du", "5")
        assertTrue(result.entries.isEmpty())
        assertEquals(listOf(1, 2, 3, 4), result.problems.map { it.line })
    }

    @Test
    fun reportsDuplicatesAndKeepsFirstOccurrence() {
        val result = parse("naan 5", "enna", "naan 9", "\u0163a", "t\u0327a")
        assertEquals(listOf(Dictionary.Entry("naan", 5), Dictionary.Entry("enna", 1), Dictionary.Entry("\u0163a", 1)), result.entries)
        assertEquals(listOf(3, 5), result.problems.map(Problem::line))
        assertTrue(result.problems[0].message.contains("line 1"))
    }

    @Test
    fun lineNumbersCountCommentsAndBlankLines() {
        val result = parse("# comment", "", "bad!")
        assertEquals(3, result.problems.single().line)
    }

    @Test
    fun wordsThatDifferOnlyByShiftAreDistinct() {
        val result = parse("ta\u01F9i", "\u0163a\u0146i")
        assertEquals(2, result.entries.size)
        assertTrue(result.problems.isEmpty())
    }
}
