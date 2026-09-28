package com.example.standardspokentamilkeyboard

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        tintGlobeIcon(findViewById(R.id.instructions))

        findViewById<Button>(R.id.enable_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        findViewById<Button>(R.id.select_button).setOnClickListener {
            getSystemService(InputMethodManager::class.java).showInputMethodPicker()
        }
    }

    /** Replaces the colour emoji 🌐 with the keyboard's globe icon, tinted to match the text. */
    private fun tintGlobeIcon(view: TextView) {
        val globe = "\uD83C\uDF10"
        val text = SpannableString(view.text)
        var start = text.indexOf(globe)
        while (start >= 0) {
            val icon = requireNotNull(ContextCompat.getDrawable(this, R.drawable.ic_kb_globe)).mutate().apply {
                setTint(view.currentTextColor)
                val size = view.textSize.toInt()
                setBounds(0, 0, size, size)
            }
            text.setSpan(CenteredImageSpan(icon), start, start + globe.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            start = text.indexOf(globe, start + globe.length)
        }
        view.text = text
    }

    /** An image span centred on the text glyphs (ImageSpan.ALIGN_CENTER needs API 29). */
    private class CenteredImageSpan(drawable: Drawable) : ImageSpan(drawable) {
        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int =
            drawable.bounds.right

        override fun draw(
            canvas: Canvas, text: CharSequence?, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint,
        ) {
            val metrics = paint.fontMetrics
            val iconTop = y + (metrics.ascent + metrics.descent) / 2 - drawable.bounds.height() / 2f
            canvas.withTranslation(x, iconTop) { drawable.draw(this) }
        }
    }
}
