package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import app.oreshkov.ledger.core.test.posting
import app.oreshkov.ledger.core.test.postings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Success(postings("Groceries", "Other Groceries")),
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
        var clickedPostingId: String? = null
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Success(listOf(posting())),
                onAddClick = {},
                onPostingClick = { clickedPostingId = it },
                onRetry = {}
            )
        }
        onNodeWithText("Groceries").performClick()
        assertEquals("1", clickedPostingId)
    }

    @Test
    fun clickingAdd_whileNotResumed_isDropped() = runComposeUiTest {
        var addClicked = false
        setContent {
            NotResumed {
                PostingListContent(
                    uiState = PostingListUiState.Empty,
                    onAddClick = { addClicked = true },
                    onPostingClick = {},
                    onRetry = {}
                )
            }
        }
        onNodeWithContentDescription("Add Posting").performClick()
        assertFalse(addClicked)
    }

    @Test
    fun clickingPosting_whileNotResumed_isDropped() = runComposeUiTest {
        var clickedPostingId: String? = null
        setContent {
            NotResumed {
                PostingListContent(
                    uiState = PostingListUiState.Success(listOf(posting())),
                    onAddClick = {},
                    onPostingClick = { clickedPostingId = it },
                    onRetry = {}
                )
            }
        }
        onNodeWithText("Groceries").performClick()
        assertNull(clickedPostingId)
    }

    @Test
    fun successState_scrollsToLaterPosting() = runComposeUiTest {
        val items = postings(30)
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Success(items),
                onAddClick = {},
                onPostingClick = {},
                onRetry = {}
            )
        }
        onNodeWithTag("posting_list").performScrollToIndex(29)
        onNodeWithText("Posting 30").assertIsDisplayed()
    }

    @Test
    fun scrollToTopRequest_scrollsBackToFirstPosting() = runComposeUiTest {
        val scrollToTopRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Success(postings(30)),
                onAddClick = {},
                onPostingClick = {},
                onRetry = {},
                scrollToTopRequests = scrollToTopRequests,
            )
        }
        onNodeWithTag("posting_list").performScrollToIndex(29)
        onNodeWithText("Posting 1").assertDoesNotExist()

        // Emitted once composition is idle, so the list's collector is already subscribed.
        runOnIdle { assertTrue(scrollToTopRequests.tryEmit(Unit)) }

        onNodeWithText("Posting 1").assertIsDisplayed()
    }

    @Test
    fun loadingState_showsProgressIndicator() = runComposeUiTest {
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Loading,
                onAddClick = {},
                onPostingClick = {},
                onRetry = {}
            )
        }
        onNodeWithTag("loading").assertIsDisplayed()
    }
}