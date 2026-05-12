package app.oreshkov.ledger.feature.posting.impl

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PostingEditUiStateTest {

    @Test
    fun isValid_isTrueWhenNarrativeIsFilled() {
        val state = PostingEditUiState.Editing(
            narrative = "Monthly rent"
        )
        assertTrue(state.isValid)
    }

    @Test
    fun isValid_isFalseWhenNarrativeIsBlank() {
        val state = PostingEditUiState.Editing(
            narrative = ""
        )
        assertFalse(state.isValid)
    }

    @Test
    fun narrativeError_isTrueOnlyWhenTouchedAndBlank() {
        assertFalse(PostingEditUiState.Editing().narrativeError)
        assertFalse(PostingEditUiState.Editing(narrative = "X", narrativeTouched = true).narrativeError)
        assertTrue(PostingEditUiState.Editing(narrativeTouched = true).narrativeError)
    }
}