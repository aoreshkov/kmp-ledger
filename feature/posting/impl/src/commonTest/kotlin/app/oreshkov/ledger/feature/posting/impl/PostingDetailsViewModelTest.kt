package app.oreshkov.ledger.feature.posting.impl

import app.oreshkov.ledger.core.domain.DeletePostingUseCase
import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.test.FakePostingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PostingDetailsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repo = FakePostingRepository()
    private val getPostingUseCase = GetPostingUseCase(repo)
    private val deletePostingUseCase = DeletePostingUseCase(repo)
    private val posting  = Posting(1, "Monthly rent")

    @BeforeTest fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun uiState_isSuccessWhenPostingExists() = runTest {
        repo.seed(posting)
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, posting.id)
        val state = vm.uiState.first { it !is PostingDetailsUiState.Loading }
        assertIs<PostingDetailsUiState.Success>(state)
    }

    @Test
    fun uiState_isNotFoundWhenPostingDoesNotExist() = runTest {
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, postingId = 99)
        val state = vm.uiState.first { it !is PostingDetailsUiState.Loading }
        assertIs<PostingDetailsUiState.NotFound>(state)
    }

    @Test
    fun retry_reloadsState() = runTest {
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, posting.id)
        // Wait for first response (NotFound since repo is empty)
        vm.uiState.first { it !is PostingDetailsUiState.Loading }

        repo.seed(posting)
        vm.retry()
        val state = vm.uiState.first { it is PostingDetailsUiState.Success }
        assertIs<PostingDetailsUiState.Success>(state)
    }

    @Test
    fun uiState_isErrorWhenRepositoryThrows() = runTest {
        repo.shouldThrowOnGetById = true
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, posting.id)
        val state = vm.uiState.first { it !is PostingDetailsUiState.Loading }
        assertIs<PostingDetailsUiState.Error>(state)
    }

    @Test
    fun deletePosting_sendsDeletedEvent() = runTest {
        repo.seed(posting)
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, posting.id)
        // Wait for Success state
        vm.uiState.first { it is PostingDetailsUiState.Success }

        var eventReceived = false
        val job = launch { vm.deletedEvent.collect { eventReceived = true } }

        vm.deletePosting()
        runCurrent()
        assertTrue(eventReceived)
        assertTrue(repo.deletedPostings.contains(posting))
        job.cancel()
    }
}