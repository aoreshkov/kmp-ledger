package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.test.FakePostingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import kotlin.time.Instant

class SavePostingUseCaseTest {
    private val repo = FakePostingRepository()
    private val useCase = SavePostingUseCase(repo)

    @Test
    fun `invoke with null id calls insertPosting`() = runTest {
        useCase(
            id = null,
            amount = 100L,
            timestamp = Instant.fromEpochMilliseconds(1000),
            currency = "USD",
            narrative = "Fuel"
        )

        assertEquals(1, repo.insertedPostings.size)
        assertTrue(repo.updatedPostings.isEmpty())
        assertEquals(100L, repo.insertedPostings.first().amount)
        assertEquals("USD", repo.insertedPostings.first().currency)
    }

    @Test
    fun `invoke with id calls updatePosting`() = runTest {
        useCase(
            id = 1L,
            amount = 200L,
            timestamp = Instant.fromEpochMilliseconds(1000),
            currency = "USD",
            narrative = "Fuel"
        )

        assertEquals(1, repo.updatedPostings.size)
        assertTrue(repo.insertedPostings.isEmpty())
        assertEquals(1L, repo.updatedPostings.first().id)
        assertEquals(200L, repo.updatedPostings.first().amount)
        assertEquals("USD", repo.updatedPostings.first().currency)
    }
}