package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.domain.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.NewPosting
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.test.FakePostingRepository
import app.oreshkov.ledger.core.test.posting
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeletePostingUseCaseTest {
    private val repo = FakePostingRepository()
    private val useCase = DeletePostingUseCase(repo)

    @Test
    fun `invoke calls deletePosting with correct posting`() = runTest {
        val seeded = posting()
        repo.seed(seeded)
        val result = useCase(seeded.id)

        assertTrue(result.isSuccess)
        assertEquals(1, repo.deletedPostings.size)
        assertEquals(seeded, repo.deletedPostings.first())
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        val seeded = posting()
        repo.seed(seeded)
        repo.failNextWrite = true

        val result = useCase(seeded.id)

        assertTrue(result.isFailure)
        assertTrue(repo.deletedPostings.isEmpty())
    }

    @Test
    fun `invoke rethrows CancellationException instead of capturing it`() = runTest {
        val cancellingUseCase = DeletePostingUseCase(CancellingRepository)

        // Cancellation must propagate to the caller, not become a failure Result.
        assertFailsWith<CancellationException> {
            cancellingUseCase("any-id")
        }
    }
}

private object CancellingRepository : PostingRepository {
    override suspend fun insertPosting(posting: NewPosting): Unit = throw CancellationException()
    override suspend fun deletePosting(id: String): Unit = throw CancellationException()
    override suspend fun updatePosting(posting: Posting): Unit = throw CancellationException()
    override fun getPostingById(id: String): Flow<Posting?> = emptyFlow()
    override fun getAllPostings(): Flow<List<Posting>> = emptyFlow()
}