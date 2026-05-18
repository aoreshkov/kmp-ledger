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
            narrativeTouched = true,
            narrative = ""
        )

        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }

        onNodeWithText("Narrative is required").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Error,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
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
                onNarrativeChange = {},
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
            narrative = "Groceries"
        )
        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Edit Posting").assertIsDisplayed()
        onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun typingInFields_triggersCallbacks() = runComposeUiTest {
        var newNarrative = ""
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = { newNarrative = it },
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Narrative").performTextInput("Groceries")
        assertEquals("Groceries", newNarrative)
    }

    @Test
    fun clickingSave_triggersOnSaveClick() = runComposeUiTest {
        var saveClicked = false
        val uiState = PostingEditUiState.Editing(
            narrative = "Groceries"
        )
        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = { saveClicked = true },
                onRetry = {}
            )
        }
        onNodeWithText("Save").performClick()
        assertTrue(saveClicked)
    }

    @Test
    fun notFoundState_showsNotFoundMessageAndGoBackButton() = runComposeUiTest {
        var backClicked = false
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.NotFound,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = { backClicked = true },
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Posting not found.").assertIsDisplayed()
        onNodeWithText("Go back").performClick()
        assertTrue(backClicked)
    }
}