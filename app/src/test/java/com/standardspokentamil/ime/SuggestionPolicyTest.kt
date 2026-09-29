package com.standardspokentamil.ime

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionPolicyTest {

    private val text = InputType.TYPE_CLASS_TEXT

    @Test
    fun allowsOrdinaryTextFields() {
        assertTrue(SuggestionPolicy.allows(text))
        assertTrue(SuggestionPolicy.allows(text or InputType.TYPE_TEXT_FLAG_MULTI_LINE))
        assertTrue(SuggestionPolicy.allows(text or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES))
        assertTrue(SuggestionPolicy.allows(text or InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE))
        assertTrue(SuggestionPolicy.allows(text or InputType.TYPE_TEXT_VARIATION_PERSON_NAME))
    }

    @Test
    fun rejectsNonTextClasses() {
        for (type in listOf(
            InputType.TYPE_NULL,
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME,
        )) {
            assertFalse("inputType $type", SuggestionPolicy.allows(type))
        }
    }

    @Test
    fun rejectsSensitiveAndMachineReadableVariations() {
        for (variation in listOf(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_URI,
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_FILTER,
        )) {
            assertFalse("variation $variation", SuggestionPolicy.allows(text or variation))
        }
    }

    @Test
    fun honoursNoSuggestionsFlag() {
        assertFalse(SuggestionPolicy.allows(text or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS))
    }
}
