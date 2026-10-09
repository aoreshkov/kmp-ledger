package app.oreshkov.ledger.feature.posting.impl

import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.domain.SavePostingUseCase
import app.oreshkov.ledger.core.test.FakePostingRepository
import app.oreshkov.ledger.core.test.newPosting
import app.oreshkov.ledger.core.test.posting
import kotlinx.coroutines.CompletableDeferred
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
    fun initialState_inEditMode_withMissingPosting_settlesOnNotFound() = runTest {
        // In edit mode the VM seeds Loading, then loadPosting() runs eagerly under the
        // UnconfinedTestDispatcher: the empty repo emits null synchronously, so by the time
        // the constructor returns the state has deterministically settled on NotFound.
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "1")
        assertIs<PostingEditUiState.NotFound>(vm.uiState.value)
    }

    @Test
    fun uiState_isEditingAfterLoadingSuccessfully() = runTest {
        repo.seed(posting())
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
    fun savePosting_onSuccess_marksSaved() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        vm.onNarrativeChange("Groceries")

        vm.savePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.isSaved)
        assertEquals(listOf(newPosting(narrative = "Groceries")), repo.insertedPostings)
        assertEquals(listOf("Groceries"), repo.getAllPostings().first().map { it.narrative })
    }

    @Test
    fun savePosting_inEditMode_updatesExistingPostingInPlace() = runTest {
        // Guards the VM passing its postingId through: dropping it would insert a
        // second row instead of updating the edited one.
        repo.seed(posting(id = "1", narrative = "Groceries"))
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "1")
        vm.uiState.first { it is PostingEditUiState.Editing }
        vm.onNarrativeChange("Rent")

        vm.savePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(posting(id = "1", narrative = "Rent")), repo.getAllPostings().first())
        assertTrue(repo.insertedPostings.isEmpty())
        assertTrue(vm.isSaved)
    }

    @Test
    fun savePosting_whileSaveInFlight_isIgnored() = runTest {
        // Hold the first write open so the second tap lands while it is still in flight.
        val writeGate = CompletableDeferred<Unit>()
        repo.writeGate = writeGate
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        vm.onNarrativeChange("Groceries")

        vm.savePosting()
        assertTrue((vm.uiState.value as PostingEditUiState.Editing).isSaving)
        vm.savePosting()
        writeGate.complete(Unit)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, repo.getAllPostings().first().size)
        assertTrue(vm.isSaved)
        // Success keeps isSaving: the screen is leaving and Save must stay disabled.
        assertTrue((vm.uiState.value as PostingEditUiState.Editing).isSaving)
    }

    @Test
    fun savePosting_inEditMode_whileSaveInFlight_isIgnored() = runTest {
        // An update cannot duplicate a row, so count writes instead: a second update
        // re-stamps the row and would be pushed again by anything that tracks changes.
        repo.seed(posting(id = "1", narrative = "Groceries"))
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "1")
        vm.uiState.first { it is PostingEditUiState.Editing }
        val writeGate = CompletableDeferred<Unit>()
        repo.writeGate = writeGate
        vm.onNarrativeChange("Rent")

        vm.savePosting()
        vm.savePosting()
        writeGate.complete(Unit)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(posting(id = "1", narrative = "Rent")), repo.updatedPostings)
        assertTrue(vm.isSaved)
    }

    @Test
    fun savePosting_afterFailure_canBeRetried() = runTest {
        // The guard must release on failure in practice, not just flip the flag.
        repo.failNextWrite = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        vm.onNarrativeChange("Groceries")
        vm.savePosting()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue((vm.uiState.value as PostingEditUiState.Editing).saveError)

        vm.savePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(newPosting(narrative = "Groceries")), repo.insertedPostings)
        assertTrue(vm.isSaved)
        assertFalse((vm.uiState.value as PostingEditUiState.Editing).saveError)
    }

    @Test
    fun savePosting_withInvalidInput_isNoOp() = runTest {
        // Blank narrative is invalid: savePosting() must early-return without saving.
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)

        vm.savePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.isSaved)
        val state = vm.uiState.value as PostingEditUiState.Editing
        assertFalse(state.isValid)
        assertTrue(state.narrativeError)
    }

    @Test
    fun savePosting_whenNotEditing_isNoOp() = runTest {
        // Defensive guard: when the screen isn't in Editing (here NotFound), savePosting()
        // must leave the state untouched and return before launching a write.
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "non-existent")
        vm.uiState.first { it is PostingEditUiState.NotFound }

        vm.savePosting()

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.isSaved)
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

        testDispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value as PostingEditUiState.Editing
        assertTrue(state.saveError)
        assertFalse(vm.isSaved)
        // The guard must release on failure, or the user could never retry the save.
        assertFalse(state.isSaving)
    }

    @Test
    fun onSaveErrorShown_clearsSaveError_andKeepsTheDraft() = runTest {
        repo.failNextWrite = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        vm.onNarrativeChange("Groceries")
        vm.savePosting()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue((vm.uiState.value as PostingEditUiState.Editing).saveError)

        vm.onSaveErrorShown()

        val state = vm.uiState.value as PostingEditUiState.Editing
        assertFalse(state.saveError)
        assertEquals("Groceries", state.narrative)
    }

    @Test
    fun retry_reloadsAfterError() = runTest {
        repo.shouldThrowOnGetById = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, "1")
        vm.uiState.first { it is PostingEditUiState.Error }

        repo.shouldThrowOnGetById = false
        repo.seed(posting())
        vm.retry()

        val state = vm.uiState.first { it is PostingEditUiState.Editing }
        assertIs<PostingEditUiState.Editing>(state)
    }
}

private val PostingEditViewModel.isSaved: Boolean
    get() = (uiState.value as? PostingEditUiState.Editing)?.isSaved == true
