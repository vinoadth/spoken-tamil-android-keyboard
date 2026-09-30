package com.standardspokentamil

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PronunciationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pronunciation)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pron_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        buildPronunciation(findViewById(R.id.pron_container))
        buildExamples(findViewById(R.id.pron_examples_container))
    }

    /** Fills the examples section with a Tamil Latin / IPA / Meaning / Notes table. */
    private fun buildExamples(container: LinearLayout) {
        if (EXAMPLES.isEmpty()) {
            container.addView(bodyText(getString(R.string.pron_examples_body)))
            return
        }
        val table = TableLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            addView(exampleRow(arrayOf("Tamil Latin", "IPA", "Meaning", "Notes"), header = true))
            for (row in EXAMPLES) {
                addView(exampleRow(row))
            }
        }
        container.addView(table)
    }

    private fun exampleRow(cells: Array<String>, header: Boolean = false): TableRow =
        TableRow(this).apply {
            val color = if (header) R.color.app_primary else R.color.app_on_background
            addView(cell(cells[0], sp = 14f, bold = header, color = color, stretch = true))
            addView(cell(cells[1], sp = 14f, mono = !header, bold = header, color = color, stretch = true))
            addView(cell(cells[2], sp = 14f, bold = header, color = color, stretch = true))
            // Optional notes: extra details about the example, shown next to the meaning.
            addView(cell(cells.getOrElse(3) { "" }, sp = 14f, bold = header, color = color, stretch = true))
        }

    /** Fills the pronunciation section with the letter tables and the nasal-vowel notes. */
    private fun buildPronunciation(container: LinearLayout) {
        container.addView(sectionTitle(getString(R.string.pron_vowels_title), topMarginDp = 24))
        container.addView(letterTable(VOWELS))

        container.addView(sectionTitle(getString(R.string.pron_consonants_title)))
        container.addView(letterTable(CONSONANTS))

        container.addView(sectionTitle(getString(R.string.pron_nasal_title)))
        container.addView(bodyText(getString(R.string.pron_nasal_body), topMarginDp = 12))
        container.addView(bodyText(getString(R.string.pron_nasal_rule1), topMarginDp = 12))
        container.addView(letterTable(NASAL_NG))
        container.addView(bodyText(getString(R.string.pron_nasal_rule2), topMarginDp = 12))
        container.addView(letterTable(NASAL_NNG))
        container.addView(bodyText(getString(R.string.pron_nasal_examples), topMarginDp = 12))
    }

    private fun sectionTitle(text: String, topMarginDp: Int = 20): TextView =
        TextView(this).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.app_primary))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(topMarginDp) }
        }

    private fun bodyText(text: String, topMarginDp: Int = 0): TextView =
        TextView(this).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(ContextCompat.getColor(context, R.color.app_on_background))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(topMarginDp) }
        }

    /** Builds a Letter / IPA / Sound table; the third column is optional. */
    private fun letterTable(rows: List<Array<String>>): TableLayout =
        TableLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = dp(8) }
            for (row in rows) {
                addView(letterRow(row))
            }
        }

    private fun letterRow(cells: Array<String>): TableRow =
        TableRow(this).apply {
            addView(cell(cells[0], sp = 18f, bold = true, color = R.color.app_primary, widthDp = 48))
            addView(cell(cells[1], sp = 16f, mono = true, color = R.color.app_on_background, widthDp = 64))
            if (cells.size > 2) {
                addView(cell(cells[2], sp = 14f, color = R.color.app_on_background, stretch = true))
            }
        }

    private fun cell(
        text: String,
        sp: Float,
        bold: Boolean = false,
        mono: Boolean = false,
        color: Int,
        widthDp: Int = 0,
        stretch: Boolean = false,
    ): TextView = TextView(this).apply {
        this.text = text
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        setTextColor(ContextCompat.getColor(context, color))
        if (bold) setTypeface(typeface, Typeface.BOLD)
        if (mono) typeface = Typeface.MONOSPACE
        gravity = Gravity.CENTER_VERTICAL
        val params = TableRow.LayoutParams(
            if (widthDp > 0) dp(widthDp) else TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT,
        )
        if (stretch) {
            params.width = 0
            params.weight = 1f
        }
        params.topMargin = dp(4)
        params.bottomMargin = dp(4)
        layoutParams = params
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private companion object {
        // Letter, IPA, sound. Matches ~/Downloads/alphabet.rs.
        val VOWELS = listOf(
            arrayOf("a", "a", "short open vowel"),
            arrayOf("á", "aː", "long open vowel"),
            arrayOf("i", "i", "short close front vowel"),
            arrayOf("í", "iː", "long close front vowel"),
            arrayOf("u", "u", "short close back rounded vowel"),
            arrayOf("ú", "uː", "long close back rounded vowel"),
            arrayOf("e", "e̞", "short mid front vowel"),
            arrayOf("é", "e̞ː", "long mid front vowel"),
            arrayOf("o", "o̞", "short mid back rounded vowel"),
            arrayOf("ó", "o̞ː", "long mid back rounded vowel"),
            arrayOf("à", "æ̆", "ultra-short near-open front vowel"),
            arrayOf("æ", "æː", "long near-open front vowel"),
            arrayOf("ì", "y̆", "ultra-short close front rounded vowel"),
            arrayOf("ù", "ɯ̆", "ultra-short close back unrounded vowel"),
        )

        val CONSONANTS = listOf(
            arrayOf("k", "k", "voiceless velar stop"),
            arrayOf("g", "ɡ", "voiced velar stop"),
            arrayOf("ĉ", "t̠ʃ", "voiceless post-alveolar affricate"),
            arrayOf("j", "d̠ʒ", "voiced post-alveolar affricate"),
            arrayOf("ţ", "ʈ", "voiceless retroflex stop"),
            arrayOf("ḑ", "ɖ", "voiced retroflex stop"),
            arrayOf("t", "t̪", "voiceless dental stop"),
            arrayOf("d", "d̪", "voiced dental stop"),
            arrayOf("p", "p", "voiceless bilabial stop"),
            arrayOf("b", "b", "voiced bilabial stop"),
            arrayOf("m", "m", "bilabial nasal"),
            arrayOf("ǹ", "n̪", "dental nasal"),
            arrayOf("n", "n", "alveolar nasal"),
            arrayOf("ņ", "ɳ", "retroflex nasal"),
            arrayOf("ñ", "ɲ", "palatal nasal"),
            arrayOf("ň", "ŋ", "velar nasal"),
            arrayOf("f", "f", "voiceless labiodental fricative"),
            arrayOf("ğ", "v", "voiced labiodental fricative"),
            arrayOf("s", "s", "voiceless alveolar fricative"),
            arrayOf("z", "z", "voiced alveolar fricative"),
            arrayOf("ş", "ʂ", "voiceless retroflex fricative"),
            arrayOf("h", "h", "voiceless glottal fricative"),
            arrayOf("v", "w", "labial–velar approximant"),
            arrayOf("y", "j", "palatal approximant"),
            arrayOf("r", "ɾ", "alveolar tap"),
            arrayOf("ŗ", "r", "alveolar trill"),
            arrayOf("l", "l", "alveolar lateral approximant"),
            arrayOf("ļ", "ɭ", "retroflex lateral approximant"),
            arrayOf("ǯ", "ɻ", "retroflex approximant"),
        )

        val NASAL_NG = listOf(
            arrayOf("aŋ", "ã"),
            arrayOf("áŋ", "ãː"),
            arrayOf("eŋ", "ẽ̞"),
            arrayOf("éŋ", "ẽ̞ː"),
            arrayOf("oŋ", "õ̞"),
            arrayOf("óŋ", "õ̞ː"),
            arrayOf("uŋ", "ũ"),
        )

        val NASAL_NNG = listOf(
            arrayOf("aňg", "ãɡ"),
            arrayOf("áňg", "ãːɡ"),
            arrayOf("eňg", "ẽ̞ɡ"),
            arrayOf("éňg", "ẽ̞ːɡ"),
            arrayOf("oňg", "õ̞ɡ"),
            arrayOf("óňg", "õ̞ːɡ"),
            arrayOf("uňg", "ũɡ"),
        )

        // Tamil Latin, IPA, meaning, and optional notes. Add rows here to populate the
        // examples table; the 4th (notes) entry is optional per row.
        val EXAMPLES = listOf<Array<String>>()
    }
}
