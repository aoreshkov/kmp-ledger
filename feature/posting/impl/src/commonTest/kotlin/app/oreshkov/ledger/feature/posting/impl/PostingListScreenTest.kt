package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PostingListScreenTest : PlatformComposeUiTest() {

    @Test
    fun emptyState_showsEmptyMessage() = runComposeUiTest {
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Empty,
                onAddClick = {},
                onPostingClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("No postings added yet.").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() = runComposeUiTest {
        var retryClicked = false
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Error,
                onAddClick = {},
                onPostingClick = {},
                onRetry = { retryClicked = true }
            )
        }
        onNodeWithText("Failed to load postings.").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun successState_showsPostingsList() = runComposeUiTest {
        val postings = listOf(
            Posting("1", "Groceries"),
            Posting("2", "Other Groceries")
        )
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Success(postings),
                onAddClick = {},
                onPostingClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Groceries").assertIsDisplayed()
        onNodeWithText("Other Groceries").assertIsDisplayed()
    }

    @Test
    fun clickingAdd_triggersOnAddClick() = runComposeUiTest {
        var addClicked = false
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Empty,
                onAddClick = { addClicked = true },
                onPostingClick = {},
                onRetry = {}
            )
        }
        onNodeWithContentDescription("Add Posting").performClick()
        assertTrue(addClicked)
    }

    @Test
    fun clickingPosting_triggersOnPostingClick() = runComposeUiTest {
        val posting = Posting("1", "Groceries")
        var clickedPostingId: String? = null
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Success(listOf(posting)),
                onAddClick = {},
                onPostingClick = { clickedPostingId = it },
                onRetry = {}
            )
        }
        onNodeWithText("Groceries").performClick()
        assertEquals("1", clickedPostingId)
    }
}