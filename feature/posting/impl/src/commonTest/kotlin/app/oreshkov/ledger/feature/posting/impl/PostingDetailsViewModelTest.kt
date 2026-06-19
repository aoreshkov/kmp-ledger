package app.oreshkov.ledger.feature.posting.impl

import app.oreshkov.ledger.core.domain.DeletePostingUseCase
import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.test.FakePostingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

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
        val posting = Posting("1", "Groceries")
        repo.seed(posting)
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
        repo.seed(Posting("1", "Groceries"))
        vm.retry()

        val state = vm.uiState.first { it is PostingDetailsUiState.Success }
        assertIs<PostingDetailsUiState.Success>(state)
    }

    @Test
    fun deletePosting_emitsDeletedEventOnSuccess() = runTest {
        val posting = Posting("1", "Groceries")
        repo.seed(posting)
        val vm = PostingDetailsViewModel(getPostingUseCase, deletePostingUseCase, "1")
        vm.uiState.first { it is PostingDetailsUiState.Success }

        vm.deletePosting()
        
        vm.deletedEvent.first() // If it doesn't emit, the test will time out
    }
}