package com.standardspokentamil.dictionary

/**
 * Parses the word list format of `assets/dictionary.tsv`:
 *
 * ```
 * # comment
 * word<TAB>frequency
 * word
 * ```
 *
 * Columns may be separated by tabs or spaces. Frequency is a non-negative integer (higher = more common) and defaults to [DEFAULT_FREQUENCY].
 * Malformed lines are skipped and reported in [Result.problems] rather than failing the whole file.
 */
object DictionaryParser {

    const val DEFAULT_FREQUENCY = 1

    private val whitespace = Regex("\\s+")

    data class Problem(val line: Int, val message: String)

    data class Result(val entries: List<Dictionary.Entry>, val problems: List<Problem>)

    fun parse(lines: Sequence<String>): Result {
        val entries = mutableListOf<Dictionary.Entry>()
        val problems = mutableListOf<Problem>()
        val seen = HashMap<String, Int>()

        lines.forEachIndexed { index, raw ->
            val lineNo = index + 1
            val line = raw.removePrefix("\uFEFF").trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed

            val fields = line.split(whitespace)
            if (fields.size > 2) {
                problems += Problem(lineNo, "expected 'word frequency', found ${fields.size} columns")
                return@forEachIndexed
            }

            val word = WordNormalizer.canonical(fields[0])
            if (!WordNormalizer.isWordText(word)) {
                problems += Problem(lineNo, "'$word' must contain only letters (no spaces, digits or punctuation)")
                return@forEachIndexed
            }

            val frequency = if (fields.size == 2) {
                fields[1].toIntOrNull()?.takeIf { it >= 0 } ?: run {
                    problems += Problem(lineNo, "frequency '${fields[1]}' is not a non-negative integer")
                    return@forEachIndexed
                }
            } else {
                DEFAULT_FREQUENCY
            }

            val firstLine = seen.putIfAbsent(word, lineNo)
            if (firstLine != null) {
                problems += Problem(lineNo, "'$word' is a duplicate of line $firstLine")
                return@forEachIndexed
            }

            entries += Dictionary.Entry(word, frequency)
        }
        return Result(entries, problems)
    }
}
