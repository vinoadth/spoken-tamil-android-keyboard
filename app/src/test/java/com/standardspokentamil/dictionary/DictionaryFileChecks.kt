package com.standardspokentamil.dictionary

import com.standardspokentamil.layout.TamSstLayout
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Checks every word list file must pass; subclasses choose the file. */
abstract class DictionaryFileChecks {

    abstract val file: File

    private val parsed by lazy { file.useLines(Charsets.UTF_8) { DictionaryParser.parse(it) } }

    @Test
    fun everyLineIsWellFormed() {
        val report = parsed.problems.joinToString("\n") { "  line ${it.line}: ${it.message}" }
        assertTrue("${file.name} has problems:\n$report", parsed.problems.isEmpty())
    }

    @Test
    fun everyWordIsTypeableOnTheKeyboard() {
        val keyboard = TamSstLayout.letterKeys
            .flatMap { listOfNotNull(it.base, it.shift, it.alt, it.altShift) }
            .toSet()
        val untypeable = parsed.entries.mapNotNull { entry ->
            val missing = entry.word.codePoints().toArray()
                .map { String(Character.toChars(it)) }
                .filter { it !in keyboard }
                .distinct()
            if (missing.isEmpty()) null else "  ${entry.word}: ${missing.joinToString()}"
        }
        assertTrue(
            "${file.name} has words with characters not on the keyboard:\n${untypeable.joinToString("\n")}",
            untypeable.isEmpty(),
        )
    }
}
