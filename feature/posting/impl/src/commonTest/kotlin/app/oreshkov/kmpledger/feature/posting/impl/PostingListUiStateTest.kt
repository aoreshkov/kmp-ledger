package app.oreshkov.kmpledger.feature.posting.impl

import app.oreshkov.kmpledger.core.model.data.Posting
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

import kotlin.time.Instant

class PostingListUiStateTest {

    @Test
    fun successState_containsExpectedPostings() {
        val postings = listOf(
            Posting(1, 100L, Instant.fromEpochMilliseconds(1000), "EUR", "Monthly rent"),
            Posting(2, 200L, Instant.fromEpochMilliseconds(2000), "USD", "Grocery")
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