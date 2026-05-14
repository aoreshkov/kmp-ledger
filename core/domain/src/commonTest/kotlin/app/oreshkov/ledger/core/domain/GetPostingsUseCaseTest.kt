package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.test.FakePostingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetPostingsUseCaseTest {
    private val repo = FakePostingRepository()
    private val useCase = GetPostingsUseCase(repo)

    @Test
    fun `invoke returns flow of postings from repository`() = runTest {
        val posting1 = Posting(1L, "Groceries")
        val posting2 = Posting(2L, "Other Groceries")
        repo.seed(posting1, posting2)

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals(posting1, result[0])
        assertEquals(posting2, result[1])
    }
}