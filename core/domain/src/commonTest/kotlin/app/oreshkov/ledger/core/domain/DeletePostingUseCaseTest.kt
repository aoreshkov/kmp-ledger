package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.test.FakePostingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeletePostingUseCaseTest {
    private val repo = FakePostingRepository()
    private val useCase = DeletePostingUseCase(repo)

    @Test
    fun `invoke calls deletePosting with correct posting`() = runTest {
        val posting = Posting("1", "Groceries")
        repo.seed(posting)
        val result = useCase(posting.id)

        assertTrue(result.isSuccess)
        assertEquals(1, repo.deletedPostings.size)
        assertEquals(posting, repo.deletedPostings.first())
    }
}