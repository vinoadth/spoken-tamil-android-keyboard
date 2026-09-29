package com.example.standardspokentamilkeyboard.dictionary

import com.example.standardspokentamilkeyboard.layout.TamSstLayout
import java.text.Normalizer

object WordNormalizer {

    /** Every character of a letter key mapped to that key's unshifted character. */
    private val toBase: Map<Int, String> = buildMap {
        for (key in TamSstLayout.letterKeys) put(key.base.codePointAt(0), key.base)
        for (key in TamSstLayout.letterKeys) {
            for (level in listOfNotNull(key.shift, key.alt, key.altShift)) {
                putIfAbsent(level.codePointAt(0), key.base)
            }
        }
    }

    fun canonical(word: String): String = Normalizer.normalize(word, Normalizer.Form.NFC)

    /**
     * The lookup key of a word: what typing the same keys without Shift or long-press produces.
     * "adù" and "áḑù" share a key, so a suggestion is found even when Shift was skipped.
     */
    fun key(word: String): String = buildString {
        canonical(word).codePoints().forEach { cp ->
            append(toBase[cp] ?: String(Character.toChars(cp)).lowercase())
        }
    }

    fun isWordText(text: String): Boolean =
        text.isNotEmpty() && text.codePoints().allMatch { Character.isLetter(it) || isCombiningMark(it) }

    private fun isCombiningMark(cp: Int): Boolean = when (Character.getType(cp).toByte()) {
        Character.NON_SPACING_MARK, Character.COMBINING_SPACING_MARK, Character.ENCLOSING_MARK -> true
        else -> false
    }
}
