package com.standardspokentamil.dictionary

class Dictionary(entries: List<Entry>) {

    data class Entry(val word: String, val frequency: Int)

    private class Indexed(val entry: Entry, val key: String)

    private val byKey: List<Indexed> = entries
        .map { Indexed(it, WordNormalizer.key(it.word)) }
        .sortedBy { it.key }

    val size: Int get() = byKey.size

    val isEmpty: Boolean get() = byKey.isEmpty()

    /**
     * Words starting with [typed], ignoring Shift and long-press differences (see [WordNormalizer.key]).
     *
     * Ranking: the exact word typed, then words that start with exactly what was typed,
     * then by frequency, then shorter words first.
     */
    fun suggest(typed: String, limit: Int = DEFAULT_LIMIT): List<String> {
        if (limit <= 0) return emptyList()
        val prefix = WordNormalizer.key(typed)
        if (prefix.isEmpty()) return emptyList()
        val exact = WordNormalizer.canonical(typed)

        val start = byKey.binarySearch { it.key.compareTo(prefix) }.let { if (it < 0) -it - 1 else it }
        val matches = mutableListOf<Entry>()
        for (i in start until byKey.size) {
            if (!byKey[i].key.startsWith(prefix)) break
            matches += byKey[i].entry
        }

        return matches
            .sortedWith(
                compareByDescending<Entry> { it.word == exact }
                    .thenByDescending { it.word.startsWith(exact) }
                    .thenByDescending { it.frequency }
                    .thenBy { it.word.length }
                    .thenBy { it.word },
            )
            .take(limit)
            .map { it.word }
    }

    companion object {
        const val DEFAULT_LIMIT = 3

        val EMPTY = Dictionary(emptyList())
    }
}
