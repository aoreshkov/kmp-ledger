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
            narrative = "Groceries"
        )

        assertEquals(1, repo.insertedPostings.size)
        assertTrue(repo.updatedPostings.isEmpty())
        assertEquals("Groceries", repo.insertedPostings.first().narrative)
    }

    @Test
    fun `invoke with id calls updatePosting`() = runTest {
        useCase(
            id = "1",
            narrative = "Groceries"
        )

        assertEquals(1, repo.updatedPostings.size)
        assertTrue(repo.insertedPostings.isEmpty())
        assertEquals("1", repo.updatedPostings.first().id)
        assertEquals("Groceries", repo.updatedPostings.first().narrative)
    }

    @Test
    fun `invoke with blank narrative returns failure`() = runTest {
        val result = useCase(id = null, narrative = "  ")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(repo.insertedPostings.isEmpty())
        assertTrue(repo.updatedPostings.isEmpty())
    }
}