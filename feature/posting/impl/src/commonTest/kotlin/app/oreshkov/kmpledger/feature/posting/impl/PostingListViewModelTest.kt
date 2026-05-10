package app.oreshkov.kmpledger.feature.posting.impl

import app.oreshkov.kmpledger.core.domain.GetPostingsUseCase
import app.oreshkov.kmpledger.core.model.data.Posting
import app.oreshkov.kmpledger.core.test.FakePostingRepository
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

import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PostingListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repo = FakePostingRepository()
    private val getPostingsUseCase = GetPostingsUseCase(repo)

    @BeforeTest fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun uiState_isEmptyWhenRepositoryHasNoPostings() = runTest {
        val vm = PostingListViewModel(getPostingsUseCase)
        val state = vm.uiState.first { it !is PostingListUiState.Loading }
        assertIs<PostingListUiState.Empty>(state)
    }

    @Test
    fun uiState_isSuccessWhenRepositoryHasPostings() = runTest {
        repo.seed(Posting(1, 100L, Instant.fromEpochMilliseconds(0), "USD", "Monthly rent"))
        val vm = PostingListViewModel(getPostingsUseCase)
        val state = vm.uiState.first { it !is PostingListUiState.Loading }
        assertIs<PostingListUiState.Success>(state)
    }

    @Test
    fun uiState_updatesWhenPostingIsAdded() = runTest {
        val vm = PostingListViewModel(getPostingsUseCase)
        vm.uiState.first { it !is PostingListUiState.Loading }

        repo.insertPosting(app.oreshkov.kmpledger.core.model.data.NewPosting(100L, Instant.fromEpochMilliseconds(0), "USD", "Monthly rent"))
        val state = vm.uiState.first { it is PostingListUiState.Success }
        assertIs<PostingListUiState.Success>(state)
    }

    @Test
    fun uiState_isErrorWhenRepositoryThrows() = runTest {
        repo.shouldThrowOnGetAll = true
        val vm = PostingListViewModel(getPostingsUseCase)
        val state = vm.uiState.first { it !is PostingListUiState.Loading }
        assertIs<PostingListUiState.Error>(state)
    }

    @Test
    fun retry_reloadsAfterError() = runTest {
        repo.shouldThrowOnGetAll = true
        val vm = PostingListViewModel(getPostingsUseCase)
        vm.uiState.first { it is PostingListUiState.Error }

        repo.shouldThrowOnGetAll = false
        repo.seed(Posting(1, 100L, Instant.fromEpochMilliseconds(0), "USD", "Monthly rent"))
        vm.retry()

        val state = vm.uiState.first { it is PostingListUiState.Success }
        assertIs<PostingListUiState.Success>(state)
    }
}