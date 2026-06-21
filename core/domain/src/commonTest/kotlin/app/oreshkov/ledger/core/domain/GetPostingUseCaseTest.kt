package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.test.FakePostingRepository
import app.oreshkov.ledger.core.test.posting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetPostingUseCaseTest {
    private val repo = FakePostingRepository()
    private val useCase = GetPostingUseCase(repo)

    @Test
    fun `invoke returns posting flow from repository when posting exists`() = runTest {
        repo.seed(posting())

        val result = useCase("1").first()

        assertEquals(posting(), result)
    }

    @Test
    fun `invoke returns null flow when posting does not exist`() = runTest {
        val result = useCase("99").first()
        assertNull(result)
    }
}