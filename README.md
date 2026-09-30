# Standard Spoken Tamil Keyboard

An Android on-screen keyboard for typing Tamil in the **Standard Spoken Tamil (SST)**
romanization. The layout is a port of the X11 `in(tam_sst)` keyboard layout.

## Keyboard layout

### Lower case

```
 1   2   3   4   5   6   7   8   9   0
 ǹ   ù   e   r   t   y   u   i   o   p
   a   s   d   f   g   h   j   k   l
 ⇧   ǯ   à   ĉ   v   b   n   m   ⌫
?123  🌐  ,  [     space     ]  .  ⏎
```

### Upper case

```
 !   @   #   $   %   ^   &   *   (   )
 ņ   è   é   ŗ   ţ   ì   ú   í   ó   p
   á   ş   ḑ   f   ğ   h   ñ   k   ļ
 ⇧   z   æ   ĉ   v   b   ň   ŋ   ⌫
?123  🌐  ,  [     space     ]  .  ⏎
```

## Using the keyboard

- **⇧ Shift**: tap once for a single upper-case letter; double-tap for caps lock.
- **Long-press** a letter to pick any of its characters, including the plain
  ASCII letter shown in the key's corner.
- **🌐 Globe**: tap to return to your previous keyboard; long-press to choose
  another keyboard. Hidden when no other keyboard is enabled.
- **?123**: numbers and symbols.
- **⌫ Backspace**: hold to delete repeatedly.

## Pronunciation

The keyboard writes Tamil in a Latin alphabet with diacritics, where each letter
maps to a single Standard Spoken Tamil sound. The tables below give the
[IPA](https://en.wikipedia.org/wiki/International_Phonetic_Alphabet) value of every
letter. Long-press a key to reach its diacritic forms.

### Vowels

| Letter | IPA | Sound |
| :----: | :-: | ----- |
| a | `a`  | short open vowel |
| á | `aː` | long open vowel |
| i | `i`  | short close front vowel |
| í | `iː` | long close front vowel |
| u | `u`  | short close back rounded vowel |
| ú | `uː` | long close back rounded vowel |
| e | `e̞`  | short mid front vowel |
| é | `e̞ː` | long mid front vowel |
| o | `o̞`  | short mid back rounded vowel |
| ó | `o̞ː` | long mid back rounded vowel |
| à | `æ̆`  | ultra-short near-open front vowel |
| æ | `æː` | long near-open front vowel |
| ì | `y̆`  | ultra-short close front rounded vowel |
| ù | `ɯ̆`  | ultra-short close back unrounded vowel |

### Consonants

| Letter | IPA | Sound |
| :----: | :-: | ----- |
| k | `k`   | voiceless velar stop |
| g | `ɡ`   | voiced velar stop |
| ĉ | `t̠ʃ`  | voiceless post-alveolar affricate |
| j | `d̠ʒ`  | voiced post-alveolar affricate |
| ţ | `ʈ`   | voiceless retroflex stop |
| ḑ | `ɖ`   | voiced retroflex stop |
| t | `t̪`   | voiceless dental stop |
| d | `d̪`   | voiced dental stop |
| p | `p`   | voiceless bilabial stop |
| b | `b`   | voiced bilabial stop |
| m | `m`   | bilabial nasal |
| ǹ | `n̪`   | dental nasal |
| n | `n`   | alveolar nasal |
| ņ | `ɳ`   | retroflex nasal |
| ñ | `ɲ`   | palatal nasal |
| ň | `ŋ`   | velar nasal |
| f | `f`   | voiceless labiodental fricative |
| ğ | `v`   | voiced labiodental fricative |
| s | `s`   | voiceless alveolar fricative |
| z | `z`   | voiced alveolar fricative |
| ş | `ʂ`   | voiceless retroflex fricative |
| h | `h`   | voiceless glottal fricative |
| v | `w`   | labial–velar approximant |
| y | `j`   | palatal approximant |
| r | `ɾ`   | alveolar tap |
| ŗ | `r`   | alveolar trill |
| l | `l`   | alveolar lateral approximant |
| ļ | `ɭ`   | retroflex lateral approximant |
| ǯ | `ɻ`   | retroflex approximant |

### Nasal vowels

Spoken Tamil often nasalises a vowel instead of pronouncing a full nasal
consonant. Two spellings trigger this, and in both the nasalisation shows up in
IPA as a tilde over the vowel (◌̃):

1. **Vowel + `ŋ`** — a `ŋ` written after a vowel is not a separate consonant. It
   nasalises the vowel before it while keeping that vowel's quality and length:

   | Spelling | IPA | | Spelling | IPA |
   | :------: | :-: | - | :------: | :-: |
   | aŋ | `ã`  | | óŋ | `õ̞ː` |
   | áŋ | `ãː` | | uŋ | `ũ`  |
   | eŋ | `ẽ̞`  | | | |
   | éŋ | `ẽ̞ː` | | | |
   | oŋ | `õ̞`  | | | |

2. **Vowel + `ň` + `g`** — on its own `ň` is the velar nasal `ŋ`. But when it
   stands between a vowel and `g`, the vowel is nasalised and a full `ɡ` still
   follows:

   | Spelling | IPA | | Spelling | IPA |
   | :------: | :--: | - | :------: | :--: |
   | aňg | `ãɡ`  | | óňg | `õ̞ːɡ` |
   | áňg | `ãːɡ` | | uňg | `ũɡ`  |
   | eňg | `ẽ̞ɡ`  | | | |
   | éňg | `ẽ̞ːɡ` | | | |
   | oňg | `õ̞ɡ`  | | | |

For example, **avaŋ** is `/awã/`, **póňgà** is `/põ̞ːɡæ̆/`, and **varùváŋ** is
`/waɾɯ̆wãː/`.

### Examples

<!-- Add worked examples below; keep the table format and fill in the rows. -->

| Tamil Latin | IPA | Meaning | Notes |
| ----------- | --- | ------- | ----- |
|             |     |         |       |
|             |     |         |       |
|             |     |         |       |

## Setup

1. Install the app and open **Standard Spoken Tamil Keyboard**.
2. Tap **Enable keyboard** and turn on *Tamil (Standard Spoken Tamil)*.
3. Tap **Select keyboard** and choose it.
4. Try it in the text field on the same screen.

## Building

Requires Android Studio (or the Android SDK) and JDK 17+. The app supports
Android 8.0 (API 26) and later.

```bash
./gradlew assembleDebug        # build the APK
./gradlew testDebugUnitTest    # run unit tests
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Building the word list

Suggestions come from `app/src/main/assets/dictionary.tsv`, one `word<TAB>frequency`
per line. `dictionary-builder/build_dictionary.py` (Python 3, no dependencies) builds
this list from raw Spoken Tamil text:

```bash
python3 dictionary-builder/build_dictionary.py corpus.txt                 # writes sample-dictionary.tsv
python3 dictionary-builder/build_dictionary.py a.txt b.txt --min-count 2  # several files, drop rare words
python3 dictionary-builder/build_dictionary.py corpus.txt --prod=true     # writes the app's dictionary.tsv
```

- Only words made entirely of letters on this keyboard are kept. Tamil-script words,
  emoji, digits, punctuation, URLs, e-mail addresses and @mentions are dropped.
- Frequency is the number of times a word occurs across all input files. Each word
  is written once; the keyboard ranks suggestions by frequency, so the file is not sorted.
- Words are lowercased by default. With `--keep-case`, spellings that differ only in
  case are merged into the most frequent one.
- `--min-length` (default 2) drops shorter words; `--min-count` (default 1) drops rarer ones.

By default the output is a draft, `sample-dictionary.tsv` in the project root
(git-ignored), which `./gradlew testDebugUnitTest` validates when present.
Pass `--prod=true` (or just `--prod`) to overwrite `app/src/main/assets/dictionary.tsv`
directly so the next build ships the new words; `-o FILE` writes anywhere else.

## License

[MIT](LICENSE) © 2026 Vinoth G <vinoth@mail.ru>
