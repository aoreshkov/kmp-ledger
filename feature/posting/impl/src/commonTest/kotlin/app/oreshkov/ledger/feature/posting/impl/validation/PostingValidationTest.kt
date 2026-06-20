package app.oreshkov.ledger.feature.posting.impl.validation

import app.oreshkov.ledger.feature.posting.impl.PostingEditUiState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PostingValidationTest {

    @Test
    fun editingState_isValid_whenNarrativeIsNotBlank() {
        val state = PostingEditUiState.Editing(narrative = "Groceries")
        assertTrue(state.isValid)
    }

    @Test
    fun editingState_isInvalid_whenNarrativeIsBlank() {
        val state = PostingEditUiState.Editing(narrative = "")
        assertFalse(state.isValid)
    }

    @Test
    fun editingState_isInvalid_whenNarrativeIsOnlyWhitespace() {
        val state = PostingEditUiState.Editing(narrative = "   ")
        assertFalse(state.isValid)
    }

    @Test
    fun narrativeError_isShown_onlyWhenTouchedAndBlank() {
        val untouchedBlank = PostingEditUiState.Editing(narrative = "", narrativeTouched = false)
        assertFalse(untouchedBlank.narrativeError)

        val touchedBlank = PostingEditUiState.Editing(narrative = "", narrativeTouched = true)
        assertTrue(touchedBlank.narrativeError)

        val touchedNotBlank = PostingEditUiState.Editing(narrative = "Groceries", narrativeTouched = true)
        assertFalse(touchedNotBlank.narrativeError)
    }
}