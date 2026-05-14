package app.oreshkov.ledger.feature.posting.impl

import app.oreshkov.ledger.core.model.data.Posting
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PostingListUiStateTest {

    @Test
    fun successState_containsExpectedPostings() {
        val postings = listOf(
            Posting(1, "Groceries"),
            Posting(2, "Other Groceries")
        )
        val state = PostingListUiState.Success(postings)
        assertEquals(postings, state.postings)
    }

    @Test
    fun otherStates_areObjects() {
        assertIs<PostingListUiState.Loading>(PostingListUiState.Loading)
        assertIs<PostingListUiState.Empty>(PostingListUiState.Empty)
        assertIs<PostingListUiState.Error>(PostingListUiState.Error)
    }
}