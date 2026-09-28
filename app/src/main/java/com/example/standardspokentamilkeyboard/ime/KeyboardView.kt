package com.example.standardspokentamilkeyboard.ime

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.text.TextPaint
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.standardspokentamilkeyboard.R
import com.example.standardspokentamilkeyboard.layout.KeyDef
import com.example.standardspokentamilkeyboard.layout.TamSstLayout

@SuppressLint("ViewConstructor")
class KeyboardView(context: Context, private val listener: Listener) : View(context) {

    interface Listener {
        fun onText(text: String)
        fun onBackspace()
        fun onEnter()
        fun onSwitchKeyboard()
        fun onShowKeyboardPicker()
        fun onSuggestionPicked(word: String)
    }

    enum class Mode { TAMIL, SYMBOLS }

    private enum class ShiftState { OFF, ONCE, LOCKED }

    private enum class KeyType { CHAR, SHIFT, BACKSPACE, SYMBOLS, SWITCH_IME, SPACE, ENTER, SPACER, SUGGESTION }

    private class SoftKey(val type: KeyType, val def: KeyDef? = null, val weight: Float = 1f) {
        val bounds = RectF()
    }

    var enterLabel: String = "\u23CE"
        set(value) {
            field = value
            invalidate()
        }

    /** Whether the globe key is shown; hidden when there is no other keyboard to switch to. */
    var showSwitchKey: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            layoutKeys()
            invalidate()
        }

    /** Whether the suggestion bar above the keys is shown; hidden while the dictionary is empty. */
    var showSuggestionStrip: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
            layoutKeys()
            invalidate()
        }

    var suggestions: List<String> = emptyList()
        set(value) {
            val trimmed = value.take(SUGGESTION_SLOTS)
            if (field == trimmed) return
            field = trimmed
            if (pressedKey?.type == KeyType.SUGGESTION) cancelPress()
            invalidate()
        }

    private val suggestionKeys: List<SoftKey> = List(SUGGESTION_SLOTS) { SoftKey(KeyType.SUGGESTION) }

    private var mode = Mode.TAMIL
    private var shiftState = ShiftState.OFF
    private var lastShiftTap = 0L

    private val letterRows: List<List<SoftKey>> = listOf(
        TamSstLayout.numberRow.map { SoftKey(KeyType.CHAR, it) },
        TamSstLayout.topRow.map { SoftKey(KeyType.CHAR, it) },
        listOf(SoftKey(KeyType.SPACER, weight = 0.5f)) +
            TamSstLayout.homeRow.map { SoftKey(KeyType.CHAR, it) } +
            SoftKey(KeyType.SPACER, weight = 0.5f),
        listOf(SoftKey(KeyType.SHIFT, weight = 1.5f)) +
            TamSstLayout.bottomRow.map { SoftKey(KeyType.CHAR, it) } +
            SoftKey(KeyType.BACKSPACE, weight = 1.5f),
        bottomControls(),
    )

    private val symbolRows: List<List<SoftKey>> = listOf(
        symbolKeys("1234567890"),
        symbolKeys("!@#$%^&*()"),
        symbolKeys("-_=+[]{}\\|"),
        symbolKeys("`~;:'\"<>/?") + SoftKey(KeyType.BACKSPACE, weight = 1.5f),
        bottomControls(),
    )

    private var rows = letterRows

    private val res = context.resources
    private val keyHeight = res.getDimension(R.dimen.kb_key_height)
    private val keyGap = res.getDimension(R.dimen.kb_key_gap)
    private val keyRadius = res.getDimension(R.dimen.kb_key_radius)
    private val keyboardPadding = res.getDimension(R.dimen.kb_padding)
    private val suggestionHeight = res.getDimension(R.dimen.kb_suggestion_height)
    private val stripHeight get() = if (showSuggestionStrip) suggestionHeight else 0f

    private val backgroundColor = ContextCompat.getColor(context, R.color.kb_background)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyColor = ContextCompat.getColor(context, R.color.kb_key)
    private val keyPressedColor = ContextCompat.getColor(context, R.color.kb_key_pressed)
    private val modifierColor = ContextCompat.getColor(context, R.color.kb_key_modifier)
    private val accentColor = ContextCompat.getColor(context, R.color.kb_key_accent)
    private val popupColor = ContextCompat.getColor(context, R.color.kb_popup)

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.kb_key_text)
        textAlign = Paint.Align.CENTER
        textSize = res.getDimension(R.dimen.kb_key_text_size)
    }
    private val modifierLabelPaint = Paint(labelPaint).apply {
        textSize = res.getDimension(R.dimen.kb_modifier_text_size)
    }
    private val suggestionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = labelPaint.color
        textAlign = Paint.Align.CENTER
        textSize = res.getDimension(R.dimen.kb_suggestion_text_size)
    }
    private val dividerColor = ContextCompat.getColor(context, R.color.kb_suggestion_divider)
    private val dividerWidth = res.getDimension(R.dimen.kb_suggestion_divider_width)
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.kb_key_hint)
        textAlign = Paint.Align.RIGHT
        textSize = res.getDimension(R.dimen.kb_hint_text_size)
    }

    private val globeIcon = requireNotNull(ContextCompat.getDrawable(context, R.drawable.ic_kb_globe))
        .mutate().apply { setTint(labelPaint.color) }
    private val iconSize = res.getDimension(R.dimen.kb_icon_size)

    private val drawRect = RectF()

    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var pressedKey: SoftKey? = null
    private var popupOptions: List<String> = emptyList()
    private var popupSelected = 0
    private val popupCells = mutableListOf<RectF>()
    private val popupFrame = RectF()

    private val isPopupShown get() = popupOptions.isNotEmpty()

    private val longPressTimeout = 400L
    private val repeatStartDelay = 400L
    private val repeatInterval = 50L

    private val longPressRunnable = Runnable {
        val key = pressedKey ?: return@Runnable
        if (key.type == KeyType.SWITCH_IME) {
            cancelPress()
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            listener.onShowKeyboardPicker()
        } else {
            showPopup(key)
        }
    }

    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (pressedKey?.type != KeyType.BACKSPACE) return
            listener.onBackspace()
            postDelayed(this, repeatInterval)
        }
    }

    init {
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            if (v.paddingBottom != bars.bottom) {
                v.setPadding(0, 0, 0, bars.bottom)
                v.requestLayout()
            }
            insets
        }
    }

    fun reset(startMode: Mode) {
        cancelPress()
        shiftState = ShiftState.OFF
        setMode(startMode)
    }

    private fun bottomControls(): List<SoftKey> = listOf(
        SoftKey(KeyType.SYMBOLS, weight = 1.5f),
        SoftKey(KeyType.SWITCH_IME, weight = 1.5f),
        SoftKey(KeyType.CHAR, KeyDef("AB08", ",", ",")),
        SoftKey(KeyType.SPACE, weight = 4f),
        SoftKey(KeyType.CHAR, KeyDef("AB09", ".", ".")),
        SoftKey(KeyType.ENTER, weight = 1.5f),
    )

    private fun symbolKeys(chars: String): List<SoftKey> =
        chars.map { SoftKey(KeyType.CHAR, KeyDef("SYM", it.toString(), it.toString())) }

    private fun setMode(newMode: Mode) {
        mode = newMode
        rows = if (newMode == Mode.SYMBOLS) symbolRows else letterRows
        layoutKeys()
        invalidate()
    }

    private val isShifted get() = shiftState != ShiftState.OFF

    private fun primaryText(key: SoftKey): String {
        val def = key.def ?: return ""
        return when (mode) {
            Mode.TAMIL -> if (isShifted) def.shift else def.base
            Mode.SYMBOLS -> def.base
        }
    }

    /** The key's plain ASCII character, shown as a hint and preselected on long-press. */
    private fun alternateText(key: SoftKey): String? {
        val def = key.def ?: return null
        if (def.alt == null || mode == Mode.SYMBOLS) return null
        val alternate = if (isShifted) def.altShift else def.alt
        return alternate?.takeIf { it != primaryText(key) }
    }

    /** All distinct characters of a four-level key, in X11 level order, offered on long-press. */
    private fun longPressOptions(key: SoftKey): List<String> {
        val def = key.def ?: return emptyList()
        if (def.alt == null || mode == Mode.SYMBOLS) return emptyList()
        val options = listOfNotNull(def.base, def.shift, def.alt, def.altShift).distinct()
        return if (options.size > 1) options else emptyList()
    }

    private fun showPopup(key: SoftKey) {
        val options = longPressOptions(key)
        if (options.isEmpty()) return
        popupOptions = options
        val primary = primaryText(key)
        popupSelected = options.indexOf(alternateText(key)).takeIf { it >= 0 }
            ?: options.indexOfFirst { it != primary }

        val cellWidth = key.bounds.width()
        val totalWidth = cellWidth * options.size
        var left = (key.bounds.centerX() - totalWidth / 2).coerceIn(0f, width - totalWidth)
        val top = (key.bounds.top - keyHeight).coerceAtLeast(0f)
        popupFrame.set(left, top, left + totalWidth, top + keyHeight)
        popupCells.clear()
        repeat(options.size) {
            popupCells += RectF(left, top, left + cellWidth, top + keyHeight)
            left += cellWidth
        }
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        invalidate()
    }

    private fun selectPopupCell(x: Float) {
        val index = popupCells.indexOfFirst { x < it.right }.let { if (it < 0) popupCells.lastIndex else it }
        if (index != popupSelected) {
            popupSelected = index
            invalidate()
        }
    }

    private fun hidePopup() {
        popupOptions = emptyList()
        popupCells.clear()
    }

    private fun labelFor(key: SoftKey): String = when (key.type) {
        KeyType.CHAR -> primaryText(key)
        KeyType.SHIFT -> if (shiftState == ShiftState.LOCKED) "\u21EA" else "\u21E7"
        KeyType.BACKSPACE -> "\u232B"
        KeyType.SYMBOLS -> if (mode == Mode.SYMBOLS) res.getString(R.string.kb_letters) else "?123"
        KeyType.SWITCH_IME -> ""
        KeyType.SPACE -> res.getString(R.string.kb_space_tamil)
        KeyType.ENTER -> enterLabel
        KeyType.SPACER -> ""
        KeyType.SUGGESTION -> suggestions.getOrNull(suggestionKeys.indexOf(key)).orEmpty()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = stripHeight + rows.size * keyHeight + 2 * keyboardPadding + paddingBottom
        setMeasuredDimension(width, height.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        layoutKeys()
    }

    private fun layoutKeys() {
        val usableWidth = width - 2 * keyboardPadding
        if (usableWidth <= 0) return
        val slotWidth = usableWidth / SUGGESTION_SLOTS
        suggestionKeys.forEachIndexed { i, key ->
            val left = keyboardPadding + i * slotWidth
            key.bounds.set(left, keyboardPadding, left + slotWidth, keyboardPadding + stripHeight)
        }
        rows.forEachIndexed { rowIndex, row ->
            val unit = usableWidth / row.sumOf { weightOf(it).toDouble() }.toFloat()
            val top = keyboardPadding + stripHeight + rowIndex * keyHeight
            var x = keyboardPadding
            for (key in row) {
                val w = weightOf(key) * unit
                key.bounds.set(x, top, x + w, top + keyHeight)
                x += w
            }
        }
    }

    private fun weightOf(key: SoftKey): Float = if (isHidden(key)) 0f else key.weight

    private fun isHidden(key: SoftKey): Boolean = key.type == KeyType.SWITCH_IME && !showSwitchKey

    private fun isInteractive(key: SoftKey): Boolean = when (key.type) {
        KeyType.SPACER -> false
        KeyType.SUGGESTION -> showSuggestionStrip && suggestionKeys.indexOf(key) < suggestions.size
        else -> !isHidden(key)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        drawSuggestions(canvas)
        val half = keyGap / 2
        for (row in rows) for (key in row) {
            if (!isInteractive(key)) continue
            val r = drawRect.apply { set(key.bounds); inset(half, half) }
            keyPaint.color = when {
                key === pressedKey -> keyPressedColor
                key.type == KeyType.SHIFT && isShifted -> accentColor
                key.type == KeyType.ENTER -> accentColor
                key.type == KeyType.CHAR || key.type == KeyType.SPACE -> keyColor
                else -> modifierColor
            }
            canvas.drawRoundRect(r, keyRadius, keyRadius, keyPaint)

            if (key.type == KeyType.SWITCH_IME) {
                val halfIcon = iconSize / 2
                globeIcon.setBounds(
                    (r.centerX() - halfIcon).toInt(), (r.centerY() - halfIcon).toInt(),
                    (r.centerX() + halfIcon).toInt(), (r.centerY() + halfIcon).toInt(),
                )
                globeIcon.draw(canvas)
                continue
            }

            val paint = if (key.type == KeyType.CHAR) labelPaint else modifierLabelPaint
            val baseline = r.centerY() - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(labelFor(key), r.centerX(), baseline, paint)

            alternateText(key)?.let { hint ->
                canvas.drawText(hint, r.right - half * 2, r.top - hintPaint.ascent() + half, hintPaint)
            }
        }
        drawPopup(canvas)
    }

    private fun drawSuggestions(canvas: Canvas) {
        if (!showSuggestionStrip) return
        val half = keyGap / 2
        suggestionKeys.forEachIndexed { i, key ->
            val text = suggestions.getOrNull(i) ?: return@forEachIndexed
            val r = drawRect.apply { set(key.bounds); inset(half, half) }
            if (key === pressedKey) {
                keyPaint.color = keyPressedColor
                canvas.drawRoundRect(r, keyRadius, keyRadius, keyPaint)
            }
            val label = TextUtils.ellipsize(text, suggestionPaint, r.width() - keyGap * 2, TextUtils.TruncateAt.END)
            val baseline = r.centerY() - (suggestionPaint.descent() + suggestionPaint.ascent()) / 2
            canvas.drawText(label, 0, label.length, r.centerX(), baseline, suggestionPaint)
        }
        keyPaint.color = dividerColor
        for (i in 1 until SUGGESTION_SLOTS) {
            val x = suggestionKeys[i].bounds.left
            val inset = stripHeight / 4
            canvas.drawRect(x - dividerWidth / 2, keyboardPadding + inset, x + dividerWidth / 2, keyboardPadding + stripHeight - inset, keyPaint)
        }
    }

    private fun drawPopup(canvas: Canvas) {
        if (!isPopupShown) return
        keyPaint.color = popupColor
        canvas.drawRoundRect(popupFrame, keyRadius, keyRadius, keyPaint)
        val half = keyGap / 2
        popupOptions.forEachIndexed { i, text ->
            val cell = drawRect.apply { set(popupCells[i]); inset(half, half) }
            if (i == popupSelected) {
                keyPaint.color = accentColor
                canvas.drawRoundRect(cell, keyRadius, keyRadius, keyPaint)
            }
            val baseline = cell.centerY() - (labelPaint.descent() + labelPaint.ascent()) / 2
            canvas.drawText(text, cell.centerX(), baseline, labelPaint)
        }
    }

    private fun keyAt(x: Float, y: Float): SoftKey? =
        (suggestionKeys.asSequence() + rows.asSequence().flatten())
            .firstOrNull { isInteractive(it) && it.bounds.contains(x, y) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (activePointerId != MotionEvent.INVALID_POINTER_ID) release()
                val index = event.actionIndex
                activePointerId = event.getPointerId(index)
                press(keyAt(event.getX(index), event.getY(index)))
            }
            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(activePointerId)
                if (index >= 0 && isPopupShown) {
                    selectPopupCell(event.getX(index))
                } else if (index >= 0) {
                    val key = keyAt(event.getX(index), event.getY(index))
                    val sliding = key !== pressedKey && pressedKey?.type != KeyType.BACKSPACE
                    if (sliding && key?.type != KeyType.BACKSPACE) press(key)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == activePointerId) release()
            }
            MotionEvent.ACTION_CANCEL -> cancelPress()
        }
        return true
    }

    private fun press(key: SoftKey?) {
        removeCallbacks(longPressRunnable)
        removeCallbacks(repeatRunnable)
        hidePopup()
        pressedKey = key
        when (key?.type) {
            null -> Unit
            KeyType.CHAR -> if (longPressOptions(key).isNotEmpty()) postDelayed(longPressRunnable, longPressTimeout)
            KeyType.SWITCH_IME -> postDelayed(longPressRunnable, longPressTimeout)
            KeyType.BACKSPACE -> {
                listener.onBackspace()
                postDelayed(repeatRunnable, repeatStartDelay)
            }
            else -> Unit
        }
        if (key != null) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        invalidate()
    }

    private fun release() {
        val key = pressedKey
        val popup = popupOptions.getOrNull(popupSelected)
        cancelPress()
        when (key?.type) {
            KeyType.CHAR -> commit(popup ?: primaryText(key))
            KeyType.SPACE -> commit(" ")
            KeyType.ENTER -> listener.onEnter()
            KeyType.SHIFT -> onShiftTap()
            KeyType.SYMBOLS -> setMode(if (mode == Mode.SYMBOLS) Mode.TAMIL else Mode.SYMBOLS)
            KeyType.SWITCH_IME -> listener.onSwitchKeyboard()
            KeyType.SUGGESTION -> suggestions.getOrNull(suggestionKeys.indexOf(key))?.let(listener::onSuggestionPicked)
            KeyType.BACKSPACE, KeyType.SPACER, null -> Unit
        }
    }

    private fun cancelPress() {
        removeCallbacks(longPressRunnable)
        removeCallbacks(repeatRunnable)
        activePointerId = MotionEvent.INVALID_POINTER_ID
        pressedKey = null
        hidePopup()
        invalidate()
    }

    private fun commit(text: String) {
        listener.onText(text)
        if (shiftState == ShiftState.ONCE) {
            shiftState = ShiftState.OFF
            invalidate()
        }
    }

    private fun onShiftTap() {
        val now = SystemClock.uptimeMillis()
        shiftState = when (shiftState) {
            ShiftState.OFF -> ShiftState.ONCE
            ShiftState.ONCE -> if (now - lastShiftTap < 300) ShiftState.LOCKED else ShiftState.OFF
            ShiftState.LOCKED -> ShiftState.OFF
        }
        lastShiftTap = now
        invalidate()
    }

    companion object {
        const val SUGGESTION_SLOTS = 3
    }
}
