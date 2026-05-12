package app.oreshkov.ledger.feature.posting.impl

import app.oreshkov.ledger.core.model.data.Posting
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

import kotlin.time.Instant

class PostingDetailsUiStateTest {

    @Test
    fun successState_containsExpectedPosting() {
        val posting = Posting(1, 100L, Instant.fromEpochMilliseconds(0), "USD", "Fuel")
        val state = PostingDetailsUiState.Success(posting)
        assertEquals(posting, state.posting)
    }

    @Test
    fun otherStates_areObjects() {
        assertIs<PostingDetailsUiState.Loading>(PostingDetailsUiState.Loading)
        assertIs<PostingDetailsUiState.Error>(PostingDetailsUiState.Error)
        assertIs<PostingDetailsUiState.NotFound>(PostingDetailsUiState.NotFound)
    }
}