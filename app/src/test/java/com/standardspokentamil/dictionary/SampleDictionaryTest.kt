package com.standardspokentamil.dictionary

import org.junit.Assume.assumeTrue
import org.junit.Before
import java.io.File

/** Validates the git-ignored draft `sample-dictionary.tsv` in the project root; skipped when it is absent. */
class SampleDictionaryTest : DictionaryFileChecks() {

    override val file = File("../sample-dictionary.tsv")

    @Before
    fun requireFile() {
        assumeTrue("no ${file.absolutePath}", file.isFile)
    }
}
