package com.example.standardspokentamilkeyboard.dictionary

import org.junit.Assert.assertEquals
import org.junit.Test

class DictionaryLoaderTest {

    @Test
    fun loadsUtf8StreamAndReportsProblems() {
        val text = "# words\n\u0163a\u0146i\t5\nbad!\nenna 2\n"
        val problems = mutableListOf<DictionaryParser.Problem>()

        val dict = DictionaryLoader.load(text.byteInputStream(Charsets.UTF_8)) { problems += it }

        assertEquals(2, dict.size)
        assertEquals(listOf("\u0163a\u0146i"), dict.suggest("ta"))
        assertEquals(listOf(3), problems.map { it.line })
    }
}
