package app.oreshkov.kmpledger.feature.posting.impl

import app.oreshkov.kmpledger.core.domain.GetPostingUseCase
import app.oreshkov.kmpledger.core.domain.SavePostingUseCase
import app.oreshkov.kmpledger.core.model.data.Posting
import app.oreshkov.kmpledger.core.test.FakePostingRepository
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PostingEditViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repo = FakePostingRepository()
    private val getPostingUseCase = GetPostingUseCase(repo)
    private val savePostingUseCase = SavePostingUseCase(repo)
    private val existing = Posting(1, 100L, Instant.fromEpochMilliseconds(1000), "USD", "Monthly rent")

    @BeforeTest fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    // --- create mode ---

    @Test
    fun init_inCreateMode_prefillsTimestamp() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = null)
        val state = assertIs<PostingEditUiState.Editing>(vm.uiState.value)
        assertTrue(state.timestamp.isNotBlank())
        // Verify it's a valid Instant
        Instant.parse(state.timestamp)
    }

    @Test
    fun savePosting_insertsNewPostingAndNavigates() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = null)
        vm.onAmountChange("100"); vm.onTimestampChange("1970-01-01T00:00:01Z")
        vm.onCurrencyChange("USD"); vm.onNarrativeChange("Civic")

        var navigated = false
        val job = launch { vm.navigationEvent.collect { navigated = true } }

        vm.savePosting()
        runCurrent()
        assertTrue(navigated)
        assertEquals(1, repo.insertedPostings.size)
        assertEquals(100L, repo.insertedPostings.first().amount)
        assertEquals("USD", repo.insertedPostings.first().currency)
        job.cancel()
    }

    @Test
    fun savePosting_doesNotInsertWhenFormIsInvalid() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = null)
        vm.savePosting()
        runCurrent()
        assertTrue(repo.insertedPostings.isEmpty())
        val editing = assertIs<PostingEditUiState.Editing>(vm.uiState.value)
        assertTrue(editing.amountError)
        assertTrue(editing.currencyError)
    }

    // --- edit mode ---

    @Test
    fun init_populatesFieldsFromRepository() = runTest {
        repo.seed(existing)
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = existing.id)
        val editing = vm.uiState.first { it is PostingEditUiState.Editing } as PostingEditUiState.Editing

        assertEquals(existing.amount.toString(), editing.amount)
        assertEquals(existing.currency, editing.currency)
        assertTrue(editing.isEditMode)
    }

    @Test
    fun savePosting_updatesExistingPosting() = runTest {
        repo.seed(existing)
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = existing.id)
        vm.uiState.first { it is PostingEditUiState.Editing }
        vm.onAmountChange("200")

        var navigated = false
        val job = launch { vm.navigationEvent.collect { navigated = true } }

        vm.savePosting()
        runCurrent()
        assertTrue(navigated)
        assertEquals(200L, repo.updatedPostings.last().amount)
        job.cancel()
    }

    @Test
    fun init_showsNotFoundWhenPostingDoesNotExist() = runTest {
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = 99)
        vm.uiState.first { it !is PostingEditUiState.Loading }
        assertIs<PostingEditUiState.NotFound>(vm.uiState.value)
    }

    @Test
    fun init_showsErrorWhenTechnicalFailure() = runTest {
        repo.shouldThrowOnGetById = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = 1)
        vm.uiState.first { it !is PostingEditUiState.Loading }
        assertIs<PostingEditUiState.Error>(vm.uiState.value)
    }

    // --- save error ---

    @Test
    fun savePosting_setsEditingWithSaveErrorWhenInsertFails() = runTest {
        repo.failNextWrite = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = null)
        vm.onAmountChange("100"); vm.onTimestampChange("1970-01-01T00:00:01Z")
        vm.onCurrencyChange("USD"); vm.onNarrativeChange("Monthly rent")

        vm.savePosting()
        runCurrent()

        val editing = assertIs<PostingEditUiState.Editing>(vm.uiState.value)
        assertTrue(editing.saveError)
        assertTrue(repo.insertedPostings.isEmpty())
    }

    @Test
    fun savePosting_setsEditingWithSaveErrorWhenUpdateFails() = runTest {
        repo.seed(existing)
        repo.failNextWrite = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = existing.id)
        vm.uiState.first { it is PostingEditUiState.Editing }
        vm.onAmountChange("200")

        vm.savePosting()
        runCurrent()

        val editing = assertIs<PostingEditUiState.Editing>(vm.uiState.value)
        assertTrue(editing.saveError)
        assertTrue(repo.updatedPostings.isEmpty())
    }

    @Test
    fun savePosting_clearsSaveErrorOnRetry() = runTest {
        repo.failNextWrite = true
        val vm = PostingEditViewModel(getPostingUseCase, savePostingUseCase, postingId = null)
        vm.onAmountChange("100"); vm.onTimestampChange("1970-01-01T00:00:01Z")
        vm.onCurrencyChange("USD"); vm.onNarrativeChange("Monthly rent")

        vm.savePosting()
        runCurrent()
        assertTrue((vm.uiState.value as PostingEditUiState.Editing).saveError)

        var navigated = false
        val job = launch { vm.navigationEvent.collect { navigated = true } }

        vm.savePosting()
        runCurrent()

        assertTrue(navigated)
        assertFalse((vm.uiState.value as? PostingEditUiState.Editing)?.saveError ?: false)
        job.cancel()
    }
}