package app.oreshkov.ledger.feature.posting.impl

import app.oreshkov.ledger.core.domain.DeletePostingUseCase
import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.test.FakePostingRepository
import app.oreshkov.ledger.core.test.posting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PostingDetailsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repo = FakePostingRepository()
    private val getPostingUseCase = GetPostingUseCase(repo)
    private val deletePostingUseCase = DeletePostingUseCase(repo)

    @BeforeTest fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun uiState_isSuccessWhenPostingExists() = runTest {
        repo.seed(posting())
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, "1")
        
        val state = vm.uiState.first { it !is PostingDetailsUiState.Loading }
        assertIs<PostingDetailsUiState.Success>(state)
    }

    @Test
    fun uiState_isNotFoundWhenPostingIsMissing() = runTest {
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, "non-existent")
        
        val state = vm.uiState.first { it !is PostingDetailsUiState.Loading }
        assertIs<PostingDetailsUiState.NotFound>(state)
    }

    @Test
    fun uiState_isErrorWhenRepositoryThrows() = runTest {
        repo.shouldThrowOnGetById = true
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, "1")
        
        val state = vm.uiState.first { it !is PostingDetailsUiState.Loading }
        assertIs<PostingDetailsUiState.Error>(state)
    }

    @Test
    fun retry_reloadsAfterError() = runTest {
        repo.shouldThrowOnGetById = true
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, "1")
        vm.uiState.first { it is PostingDetailsUiState.Error }

        repo.shouldThrowOnGetById = false
        repo.seed(posting())
        vm.retry()

        val state = vm.uiState.first { it is PostingDetailsUiState.Success }
        assertIs<PostingDetailsUiState.Success>(state)
    }

    @Test
    fun deletePosting_onSuccess_marksDeleted_andKeepsShowingThePosting() = runTest {
        repo.seed(posting())
        val vm = subscribedViewModel()
        vm.uiState.first { it is PostingDetailsUiState.Success }

        vm.deletePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(posting()), repo.deletedPostings)
        // The load flow now reports the row missing; the screen must not flash NotFound
        // while it navigates away.
        assertEquals(PostingDetailsUiState.Success(posting(), isDeleted = true), vm.uiState.value)
    }

    @Test
    fun deletePosting_whenNotSuccess_isNoOp() = runTest {
        // NotFound (not Success): deletePosting() must early-return without a write.
        val vm = subscribedViewModel(postingId = "non-existent")
        vm.uiState.first { it is PostingDetailsUiState.NotFound }

        vm.deletePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(repo.deletedPostings.isEmpty())
        assertIs<PostingDetailsUiState.NotFound>(vm.uiState.value)
    }

    @Test
    fun deletePosting_whenDeleteFails_setsDeleteError_andDoesNotMarkDeleted() = runTest {
        repo.seed(posting())
        val vm = subscribedViewModel()
        vm.uiState.first { it is PostingDetailsUiState.Success }

        repo.failNextWrite = true
        vm.deletePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(PostingDetailsUiState.Success(posting(), deleteError = true), vm.uiState.value)
    }

    @Test
    fun onDeleteErrorShown_clearsDeleteError() = runTest {
        repo.seed(posting())
        val vm = subscribedViewModel()
        vm.uiState.first { it is PostingDetailsUiState.Success }
        repo.failNextWrite = true
        vm.deletePosting()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onDeleteErrorShown()

        assertEquals(PostingDetailsUiState.Success(posting()), vm.uiState.value)
    }

    @Test
    fun deletePosting_afterFailure_canBeRetried() = runTest {
        repo.seed(posting())
        val vm = subscribedViewModel()
        vm.uiState.first { it is PostingDetailsUiState.Success }
        repo.failNextWrite = true
        vm.deletePosting()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.deletePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(posting()), repo.deletedPostings)
        assertEquals(PostingDetailsUiState.Success(posting(), isDeleted = true), vm.uiState.value)
    }

    @Test
    fun deletePosting_afterDeleted_isNoOp() = runTest {
        repo.seed(posting())
        val vm = subscribedViewModel()
        vm.uiState.first { it is PostingDetailsUiState.Success }
        vm.deletePosting()
        testDispatcher.scheduler.advanceUntilIdle()

        // The fake records only deletes that find a row, so detect a second write by
        // whether it consumes a pending failure instead.
        repo.failNextWrite = true
        vm.deletePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(repo.failNextWrite, "a deleted posting must not be deleted again")
        assertEquals(PostingDetailsUiState.Success(posting(), isDeleted = true), vm.uiState.value)
    }

    /**
     * Keeps [PostingDetailsViewModel.uiState] subscribed, as the screen does, so its
     * `WhileSubscribed` upstream keeps running and `uiState.value` stays current.
     */
    private fun TestScope.subscribedViewModel(postingId: String = "1"): PostingDetailsViewModel {
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, postingId)
        backgroundScope.launch(testDispatcher) { vm.uiState.collect() }
        return vm
    }
}
