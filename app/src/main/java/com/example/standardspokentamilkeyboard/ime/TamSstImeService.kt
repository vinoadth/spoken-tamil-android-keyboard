package com.example.standardspokentamilkeyboard.ime

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.example.standardspokentamilkeyboard.R
import com.example.standardspokentamilkeyboard.dictionary.Dictionary
import com.example.standardspokentamilkeyboard.dictionary.DictionaryLoader
import kotlin.concurrent.thread

class TamSstImeService : InputMethodService(), KeyboardView.Listener {

    private var keyboardView: KeyboardView? = null

    private var dictionary = Dictionary.EMPTY
    private var fieldAllowsSuggestions = false

    private val editor = object : TypingSession.Editor {
        override fun setComposingText(text: String) {
            currentInputConnection?.setComposingText(text, 1)
        }

        override fun finishComposingText() {
            currentInputConnection?.finishComposingText()
        }

        override fun commitText(text: String) {
            currentInputConnection?.commitText(text, 1)
        }

        override fun deleteBackward() {
            val ic = currentInputConnection ?: return
            when {
                !ic.getSelectedText(0).isNullOrEmpty() -> ic.commitText("", 1)
                ic.getTextBeforeCursor(1, 0).isNullOrEmpty() -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                else -> ic.deleteSurroundingTextInCodePoints(1, 0)
            }
        }
    }

    private val session = TypingSession(
        editor,
        suggest = { dictionary.suggest(it, KeyboardView.SUGGESTION_SLOTS) },
        onSuggestions = { keyboardView?.suggestions = it },
    )

    override fun onCreate() {
        super.onCreate()
        val main = Handler(Looper.getMainLooper())
        thread(name = "dictionary-loader") {
            val loaded = DictionaryLoader.load(assets)
            main.post {
                dictionary = loaded
                updateSuggestionMode()
            }
        }
    }

    override fun onCreateInputView(): View = KeyboardView(this, this).also {
        keyboardView = it
        updateSuggestionMode()
    }

    private fun updateSuggestionMode() {
        keyboardView?.showSuggestionStrip = !dictionary.isEmpty
        session.suggestionsEnabled = fieldAllowsSuggestions && !dictionary.isEmpty
    }

    /** Show the soft keyboard even when a hardware keyboard (e.g. an emulator's host keyboard) is attached. */
    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        session.reset()
        fieldAllowsSuggestions = SuggestionPolicy.allows(info.inputType)
        updateSuggestionMode()
        val view = keyboardView ?: return
        view.enterLabel = enterLabelFor(info)
        view.showSwitchKey = shouldOfferSwitchKey()
        if (!restarting) {
            val numeric = when (info.inputType and InputType.TYPE_MASK_CLASS) {
                InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_DATETIME -> true
                else -> false
            }
            view.reset(if (numeric) KeyboardView.Mode.SYMBOLS else KeyboardView.Mode.TAMIL)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        session.finishWord()
        super.onFinishInputView(finishingInput)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int,
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        session.onSelectionChanged(newSelStart, newSelEnd, candidatesStart, candidatesEnd)
    }

    override fun onText(text: String) = session.type(text)

    override fun onBackspace() = session.backspace()

    override fun onSuggestionPicked(word: String) = session.pickSuggestion(word)

    override fun onEnter() {
        session.finishWord()
        val ic = currentInputConnection ?: return
        val info = currentInputEditorInfo
        val action = editorAction(info)
        if (action != null) {
            ic.performEditorAction(action)
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    override fun onSwitchKeyboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!switchToPreviousInputMethod()) switchToNextInputMethod(false)
        } else {
            val token = window.window?.attributes?.token ?: return
            @Suppress("DEPRECATION")
            inputMethodManager.run {
                if (!switchToLastInputMethod(token)) switchToNextInputMethod(token, false)
            }
        }
    }

    override fun onShowKeyboardPicker() {
        inputMethodManager.showInputMethodPicker()
    }

    private fun shouldOfferSwitchKey(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) return shouldOfferSwitchingToNextInputMethod()
        val token = window.window?.attributes?.token ?: return true
        @Suppress("DEPRECATION")
        return inputMethodManager.shouldOfferSwitchingToNextInputMethod(token)
    }

    private val inputMethodManager: InputMethodManager
        get() = getSystemService(InputMethodManager::class.java)

    private fun editorAction(info: EditorInfo?): Int? {
        if (info == null || info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) return null
        if (info.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0) return null
        return when (val action = info.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_NONE, EditorInfo.IME_ACTION_UNSPECIFIED -> null
            else -> action
        }
    }

    private fun enterLabelFor(info: EditorInfo): String {
        val res = when (editorAction(info)) {
            EditorInfo.IME_ACTION_GO -> R.string.kb_action_go
            EditorInfo.IME_ACTION_SEARCH -> R.string.kb_action_search
            EditorInfo.IME_ACTION_SEND -> R.string.kb_action_send
            EditorInfo.IME_ACTION_NEXT -> R.string.kb_action_next
            EditorInfo.IME_ACTION_DONE -> R.string.kb_action_done
            EditorInfo.IME_ACTION_PREVIOUS -> R.string.kb_action_previous
            else -> return "\u23CE"
        }
        return getString(res)
    }
}
