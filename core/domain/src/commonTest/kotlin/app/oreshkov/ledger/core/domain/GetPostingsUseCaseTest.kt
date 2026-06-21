package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.test.FakePostingRepository
import app.oreshkov.ledger.core.test.posting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetPostingsUseCaseTest {
    private val repo = FakePostingRepository()
    private val useCase = GetPostingsUseCase(repo)

    @Test
    fun `invoke returns flow of postings from repository`() = runTest {
        val posting1 = posting()
        val posting2 = posting(id = "2", narrative = "Other Groceries")
        repo.seed(posting1, posting2)

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals(posting1, result[0])
        assertEquals(posting2, result[1])
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        repo.shouldThrowOnGetAll = true

        assertFailsWith<IllegalStateException> {
            useCase().first()
        }
    }
}