package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.test.FakePostingRepository
import app.oreshkov.ledger.core.test.posting
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
}