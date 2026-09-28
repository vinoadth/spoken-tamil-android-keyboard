package com.example.standardspokentamilkeyboard.dictionary

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Validates the word list shipped in `assets/dictionary.tsv`. */
class DictionaryAssetTest : DictionaryFileChecks() {

    override val file = File("src/main/assets/${DictionaryLoader.ASSET_NAME}")

    @Test
    fun assetExists() {
        assertTrue("missing ${file.absolutePath}", file.isFile)
    }
}
