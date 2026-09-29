package com.standardspokentamil.dictionary

import android.content.res.AssetManager
import android.util.Log
import java.io.IOException
import java.io.InputStream

object DictionaryLoader {

    const val ASSET_NAME = "dictionary.tsv"

    private const val TAG = "DictionaryLoader"

    fun load(assets: AssetManager): Dictionary = try {
        assets.open(ASSET_NAME).use { load(it) { problem -> Log.w(TAG, "$ASSET_NAME:${problem.line}: ${problem.message}") } }
    } catch (e: IOException) {
        Log.e(TAG, "Could not read $ASSET_NAME", e)
        Dictionary.EMPTY
    }

    fun load(input: InputStream, onProblem: (DictionaryParser.Problem) -> Unit = {}): Dictionary {
        val result = input.bufferedReader(Charsets.UTF_8).useLines { DictionaryParser.parse(it) }
        result.problems.forEach(onProblem)
        return Dictionary(result.entries)
    }
}
