package app.oreshkov.kmpledger.feature.posting.impl

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PostingEditUiStateTest {

    @Test
    fun isValid_isTrueWhenAllFieldsAreFilled() {
        val state = PostingEditUiState.Editing(
            amount = "100",
            timestamp = "1970-01-01T00:00:01Z",
            currency = "USD",
            narrative = "Monthly rent"
        )
        assertTrue(state.isValid)
    }

    @Test
    fun isValid_isFalseWhenAnyFieldIsBlank() {
        val state = PostingEditUiState.Editing(
            amount = "100",
            timestamp = "",
            currency = "USD",
            narrative = "Monthly rent"
        )
        assertFalse(state.isValid)
    }

    @Test
    fun amountError_isTrueOnlyWhenTouchedAndInvalid() {
        assertFalse(PostingEditUiState.Editing().amountError)
        assertFalse(PostingEditUiState.Editing(amount = "100", amountTouched = true).amountError)
        assertTrue(PostingEditUiState.Editing(amountTouched = true).amountError)
        assertTrue(PostingEditUiState.Editing(amount = "abc", amountTouched = true).amountError)
    }

    @Test
    fun currencyError_isTrueOnlyWhenTouchedAndBlank() {
        assertFalse(PostingEditUiState.Editing().currencyError)
        assertFalse(PostingEditUiState.Editing(currency = "USD", currencyTouched = true).currencyError)
        assertTrue(PostingEditUiState.Editing(currencyTouched = true).currencyError)
    }

    @Test
    fun narrativeError_isTrueOnlyWhenTouchedAndBlank() {
        assertFalse(PostingEditUiState.Editing().narrativeError)
        assertFalse(PostingEditUiState.Editing(narrative = "X", narrativeTouched = true).narrativeError)
        assertTrue(PostingEditUiState.Editing(narrativeTouched = true).narrativeError)
    }

    @Test
    fun timestampError_isTrueOnlyWhenTouchedAndInvalid() {
        assertFalse(PostingEditUiState.Editing().timestampError)
        assertFalse(PostingEditUiState.Editing(timestamp = "1970-01-01T00:00:01Z", timestampTouched = true).timestampError)
        assertTrue(PostingEditUiState.Editing(timestampTouched = true).timestampError)
        assertTrue(PostingEditUiState.Editing(timestamp = "invalid", timestampTouched = true).timestampError)
    }
}