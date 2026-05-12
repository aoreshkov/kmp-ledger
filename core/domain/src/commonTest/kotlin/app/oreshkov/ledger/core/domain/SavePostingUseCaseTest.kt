package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.test.FakePostingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SavePostingUseCaseTest {
    private val repo = FakePostingRepository()
    private val useCase = SavePostingUseCase(repo)

    @Test
    fun `invoke with null id calls insertPosting`() = runTest {
        useCase(
            id = null,
            narrative = "Fuel"
        )

        assertEquals(1, repo.insertedPostings.size)
        assertTrue(repo.updatedPostings.isEmpty())
        assertEquals("Fuel", repo.insertedPostings.first().narrative)
    }

    @Test
    fun `invoke with id calls updatePosting`() = runTest {
        useCase(
            id = 1L,
            narrative = "Fuel"
        )

        assertEquals(1, repo.updatedPostings.size)
        assertTrue(repo.insertedPostings.isEmpty())
        assertEquals(1L, repo.updatedPostings.first().id)
        assertEquals("Fuel", repo.updatedPostings.first().narrative)
    }
}