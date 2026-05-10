package app.oreshkov.kmpledger.feature.posting.impl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.kmpledger.core.model.data.Posting
import app.oreshkov.kmpledger.core.test.PlatformComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import kotlin.time.Instant

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
            Posting(1, 100L, Instant.fromEpochMilliseconds(1000), "USD", "Monthly rent"),
            Posting(2, 200L, Instant.fromEpochMilliseconds(2000), "EUR", "Grocery")
        )
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Success(postings),
                onAddClick = {},
                onPostingClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("100 USD").assertIsDisplayed()
        onNodeWithText("200 EUR").assertIsDisplayed()
        onNodeWithText(Instant.fromEpochMilliseconds(1000).toString()).assertIsDisplayed()
        onNodeWithText(Instant.fromEpochMilliseconds(2000).toString()).assertIsDisplayed()
        onNodeWithText("Monthly rent").assertIsDisplayed()
        onNodeWithText("Grocery").assertIsDisplayed()
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
        val posting = Posting(1, 100L, Instant.fromEpochMilliseconds(0), "EUR", "Monthly rent")
        var clickedPostingId: Long? = null
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Success(listOf(posting)),
                onAddClick = {},
                onPostingClick = { clickedPostingId = it },
                onRetry = {}
            )
        }
        onNodeWithText("100 EUR").performClick()
        assertEquals(1L, clickedPostingId)
    }
}