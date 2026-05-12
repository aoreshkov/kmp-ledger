package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PostingEditScreenTest : PlatformComposeUiTest() {

    @Test
    fun validationErrors_areShown_whenFieldsAreBlankAndTouched() = runComposeUiTest {
        val uiState = PostingEditUiState.Editing(
            amountTouched = true,
            timestampTouched = true,
            currencyTouched = true,
            narrativeTouched = true,
            amount = "",
            timestamp = "",
            currency = "",
            narrative = ""
        )

        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onAmountChange = {},
                onCurrencyChange = {},
                onNarrativeChange = {},
                onTimestampChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }

        onNodeWithText("Invalid amount").performScrollTo().assertIsDisplayed()
        onNodeWithText("Invalid timestamp").performScrollTo().assertIsDisplayed()
        onNodeWithText("Currency is required").performScrollTo().assertIsDisplayed()
        onNodeWithText("Narrative is required").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Error,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onAmountChange = {},
                onCurrencyChange = {},
                onNarrativeChange = {},
                onTimestampChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Failed to load posting details.").assertIsDisplayed()
    }

    @Test
    fun addMode_showsAddTitle() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(isEditMode = false),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onAmountChange = {},
                onCurrencyChange = {},
                onNarrativeChange = {},
                onTimestampChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Add Posting").assertIsDisplayed()
    }

    @Test
    fun editMode_showsEditTitleAndPopulatesFields() = runComposeUiTest {
        val uiState = PostingEditUiState.Editing(
            isEditMode = true,
            amount = "100",
            currency = "EUR",
            narrative = "Monthly rent",
            timestamp = "1970-01-01T00:00:01Z"
        )
        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onAmountChange = {},
                onCurrencyChange = {},
                onNarrativeChange = {},
                onTimestampChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Edit Posting").assertIsDisplayed()
        onNodeWithText("100").assertIsDisplayed()
        onNodeWithText("EUR").assertIsDisplayed()
        onNodeWithText("Monthly rent").assertIsDisplayed()
        onNodeWithText("1970-01-01T00:00:01Z").assertIsDisplayed()
    }

    @Test
    fun typingInFields_triggersCallbacks() = runComposeUiTest {
        var newAmount = ""
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onAmountChange = { newAmount = it },
                onCurrencyChange = {},
                onNarrativeChange = {},
                onTimestampChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Amount").performTextInput("100")
        assertEquals("100", newAmount)
    }

    @Test
    fun clickingSave_triggersOnSaveClick() = runComposeUiTest {
        var saveClicked = false
        val uiState = PostingEditUiState.Editing(
            amount = "100",
            timestamp = "1970-01-01T00:00:01Z",
            currency = "Tesla",
            narrative = "Fuel"
        )
        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onAmountChange = {},
                onCurrencyChange = {},
                onNarrativeChange = {},
                onTimestampChange = {},
                onSaveClick = { saveClicked = true },
                onRetry = {}
            )
        }
        onNodeWithText("Save").performClick()
        assertTrue(saveClicked)
    }
}