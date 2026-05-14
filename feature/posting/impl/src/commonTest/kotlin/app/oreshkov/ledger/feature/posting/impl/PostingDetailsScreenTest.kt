package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PostingDetailsScreenTest : PlatformComposeUiTest() {

    @Test
    fun errorState_showsErrorMessageAndRetryButton() = runComposeUiTest {
        var retryClicked = false
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Error,
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = {},
                onRetry = { retryClicked = true }
            )
        }
        onNodeWithText("Failed to load posting details.").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun notFoundState_showsNotFoundMessageAndGoBackButton() = runComposeUiTest {
        var backClicked = false
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.NotFound,
                onNavigateBack = { backClicked = true },
                onEditClick = {},
                onDeleteClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Posting has been already deleted.").assertIsDisplayed()
        onNodeWithText("Go back").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun successState_showsPostingDetails() = runComposeUiTest {
        val posting = Posting(
            id = 1,
            narrative = "Groceries"
        )
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun successState_clickingDelete_showsConfirmationDialog() = runComposeUiTest {
        val posting = Posting(1, "Groceries")
        var deleteConfirmed = false
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = { deleteConfirmed = true },
                onRetry = {}
            )
        }

        // Click delete icon in top bar
        onNodeWithContentDescription("Delete Posting").performClick()

        // Check dialog title
        onNodeWithText("Delete posting?").assertIsDisplayed()
        
        // Click confirm in dialog
        onNodeWithText("Delete").performClick()
        
        assertTrue(deleteConfirmed)
    }
}