package app.oreshkov.ledger.feature.posting.impl

import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.domain.SavePostingUseCase
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PostingEditViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repo = FakePostingRepository()
    private val getPostingUseCase = GetPostingUseCase(repo)
    private val savePostingUseCase = SavePostingUseCase(repo)

    @BeforeTest fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun initialState_isEditingInCreateMode() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        val state = vm.uiState.value
        assertIs<PostingEditUiState.Editing>(state)
        assertFalse(state.isEditMode)
    }

    @Test
    fun initialState_isLoadingInEditMode() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "1")
        // Initial state is Loading, but it might transition immediately with UnconfinedTestDispatcher
        // if the repository is already seeded or if it emits synchronously.
        // In this test, repo is empty, so it might transition to NotFound immediately.
        val state = vm.uiState.value
        assertTrue(state is PostingEditUiState.Loading || state is PostingEditUiState.NotFound)
    }

    @Test
    fun uiState_isEditingAfterLoadingSuccessfully() = runTest {
        repo.seed(Posting("1", "Groceries"))
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "1")
        
        val state = vm.uiState.first { it is PostingEditUiState.Editing } as PostingEditUiState.Editing
        assertTrue(state.isEditMode)
        assertEquals("Groceries", state.narrative)
    }

    @Test
    fun uiState_isNotFoundWhenLoadingMissingPosting() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "non-existent")
        
        val state = vm.uiState.first { it !is PostingEditUiState.Loading }
        assertIs<PostingEditUiState.NotFound>(state)
    }

    @Test
    fun onNarrativeChange_updatesState() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        vm.onNarrativeChange("New Narrative")
        
        val state = vm.uiState.value as PostingEditUiState.Editing
        assertEquals("New Narrative", state.narrative)
        assertTrue(state.narrativeTouched)
    }

    @Test
    fun savePosting_emitsNavigationEventOnSuccess() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        val events = backgroundScope.collectToList(vm.navigationEvent, testDispatcher)
        vm.onNarrativeChange("Groceries")

        vm.savePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, events.size)
    }

    @Test
    fun savePosting_withInvalidInput_isNoOp() = runTest {
        // Blank narrative is invalid: savePosting() must early-return without navigating.
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        val events = backgroundScope.collectToList(vm.navigationEvent, testDispatcher)

        vm.savePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(events.isEmpty())
        val state = vm.uiState.value as PostingEditUiState.Editing
        assertFalse(state.isValid)
        assertTrue(state.narrativeError)
    }

    @Test
    fun savePosting_whenNotEditing_isNoOp() = runTest {
        // Defensive guard: when the screen isn't in Editing (here NotFound), both the
        // updateEditing(else) and the `as? Editing ?: return` paths must no-op.
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "non-existent")
        vm.uiState.first { it is PostingEditUiState.NotFound }
        val events = backgroundScope.collectToList(vm.navigationEvent, testDispatcher)

        vm.savePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(events.isEmpty())
        assertIs<PostingEditUiState.NotFound>(vm.uiState.value)
    }

    @Test
    fun loadFailure_setsErrorState() = runTest {
        // Direct cover of the runCatching{}.fold(onFailure) branch in loadPosting().
        repo.shouldThrowOnGetById = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "1")

        val state = vm.uiState.first { it !is PostingEditUiState.Loading }
        assertIs<PostingEditUiState.Error>(state)
    }

    @Test
    fun savePosting_setsSaveErrorOnFailure() = runTest {
        repo.failNextWrite = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        vm.onNarrativeChange("Groceries")
        vm.savePosting()
        
        val state = vm.uiState.value as PostingEditUiState.Editing
        assertTrue(state.saveError)
    }

    @Test
    fun retry_reloadsAfterError() = runTest {
        repo.shouldThrowOnGetById = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "1")
        vm.uiState.first { it is PostingEditUiState.Error }

        repo.shouldThrowOnGetById = false
        repo.seed(Posting("1", "Groceries"))
        vm.retry()

        val state = vm.uiState.first { it is PostingEditUiState.Editing }
        assertIs<PostingEditUiState.Editing>(state)
    }
}