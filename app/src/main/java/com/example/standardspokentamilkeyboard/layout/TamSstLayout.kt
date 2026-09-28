package com.example.standardspokentamilkeyboard.layout

/**
 * One key of the X11 `in(tam_sst)` layout.
 *
 * [base] and [shift] are XKB levels 1 and 2; [alt] and [altShift] are levels 3 and 4
 * (reached via the ABC layer or long-press). Keys with only two levels leave them null.
 */
data class KeyDef(
    val id: String,
    val base: String,
    val shift: String,
    val alt: String? = null,
    val altShift: String? = null,
)

object TamSstLayout {

    const val NAME = "Tamil (Standard Spoken Tamil)"

    val numberRow: List<KeyDef> = listOf(
        KeyDef("AE01", "1", "!"),
        KeyDef("AE02", "2", "@"),
        KeyDef("AE03", "3", "#"),
        KeyDef("AE04", "4", "$"),
        KeyDef("AE05", "5", "%"),
        KeyDef("AE06", "6", "^"),
        KeyDef("AE07", "7", "&"),
        KeyDef("AE08", "8", "*"),
        KeyDef("AE09", "9", "("),
        KeyDef("AE10", "0", ")"),
    )

    val topRow: List<KeyDef> = listOf(
        KeyDef("AD01", "\u01F9", "\u0146", "q", "Q"),
        KeyDef("AD02", "\u00F9", "\u00E8", "w", "W"),
        KeyDef("AD03", "e", "\u00E9", "e", "E"),
        KeyDef("AD04", "r", "\u0157", "r", "R"),
        KeyDef("AD05", "t", "\u0163", "t", "T"),
        KeyDef("AD06", "y", "\u00EC", "y", "Y"),
        KeyDef("AD07", "u", "\u00FA", "u", "U"),
        KeyDef("AD08", "i", "\u00ED", "i", "I"),
        KeyDef("AD09", "o", "\u00F3", "o", "O"),
        KeyDef("AD10", "p", "p", "p", "P"),
    )

    val homeRow: List<KeyDef> = listOf(
        KeyDef("AC01", "a", "\u00E1", "a", "A"),
        KeyDef("AC02", "s", "\u015F", "\u015B", "S"),
        KeyDef("AC03", "d", "\u1E11", "d", "D"),
        KeyDef("AC04", "f", "f", "f", "F"),
        KeyDef("AC05", "g", "\u011F", "g", "G"),
        KeyDef("AC06", "h", "h", "h", "H"),
        KeyDef("AC07", "j", "\u00F1", "j", "J"),
        KeyDef("AC08", "k", "k", "k", "K"),
        KeyDef("AC09", "l", "\u013C", "l", "L"),
    )

    val bottomRow: List<KeyDef> = listOf(
        KeyDef("AB01", "\u01EF", "z", "z", "Z"),
        KeyDef("AB02", "\u00E0", "\u00E6", "x", "X"),
        KeyDef("AB03", "\u0109", "\u0109", "c", "C"),
        KeyDef("AB04", "v", "v", "v", "V"),
        KeyDef("AB05", "b", "b", "b", "B"),
        KeyDef("AB06", "n", "\u0148", "n", "N"),
        KeyDef("AB07", "m", "\u014B", "m", "M"),
    )

    val punctuation: List<KeyDef> = listOf(
        KeyDef("TLDE", "`", "~"),
        KeyDef("AE11", "-", "_"),
        KeyDef("AE12", "=", "+"),
        KeyDef("AD11", "[", "{"),
        KeyDef("AD12", "]", "}"),
        KeyDef("AC10", ";", ":"),
        KeyDef("AC11", "'", "\""),
        KeyDef("BKSL", "\\", "|"),
        KeyDef("AB08", ",", "<"),
        KeyDef("AB09", ".", ">"),
        KeyDef("AB10", "/", "?"),
    )

    /** The four-level letter keys, i.e. the keys whose X11 definition uses AltGr. */
    val letterKeys: List<KeyDef> = topRow + homeRow + bottomRow
}
