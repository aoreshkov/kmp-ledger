package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import app.oreshkov.ledger.core.domain.DeletePostingUseCase
import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.test.FakePostingRepository
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import app.oreshkov.ledger.core.test.posting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class PostingDetailsScreenTest : PlatformComposeUiTest() {

    // The VM-wired tests below drive viewModelScope, which dispatches on Main.
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() = runComposeUiTest {
        var retryClicked = false
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Error,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = {},
                onRetry = { retryClicked = true }
            )
        }
        onNodeWithText("Failed to load posting details.").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun notFoundState_showsNotFoundMessageAndGoBackButton() = runComposeUiTest {
        var backClicked = false
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.NotFound,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = { backClicked = true },
                onEditClick = {},
                onDeleteClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Posting has been already deleted.").assertIsDisplayed()
        onNodeWithText("Go back").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun successState_showsPostingDetails() = runComposeUiTest {
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting()),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun successState_clickingDelete_showsConfirmationDialog() = runComposeUiTest {
        var deleteConfirmed = false
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting()),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = { deleteConfirmed = true },
                onRetry = {}
            )
        }

        // Click delete icon in top bar
        onNodeWithContentDescription("Delete Posting").performClick()

        // Check dialog title
        onNodeWithText("Delete posting?").assertIsDisplayed()
        
        // Click confirm in dialog
        onNodeWithText("Delete").performClick()
        
        assertTrue(deleteConfirmed)
    }

    @Test
    fun successState_clickingDelete_cancelDismissesDialog() = runComposeUiTest {
        var deleteConfirmed = false
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting()),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = { deleteConfirmed = true },
                onRetry = {}
            )
        }

        onNodeWithContentDescription("Delete Posting").performClick()
        onNodeWithText("Delete posting?").assertIsDisplayed()
        
        onNodeWithText("Cancel").performClick()
        
        // Dialog should be gone
        onNodeWithText("Delete posting?").assertDoesNotExist()
        assertFalse(deleteConfirmed)
    }

    @Test
    fun successState_clickingEdit_triggersOnEditClickWithCorrectId() = runComposeUiTest {
        var editClickedId: String? = null
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting(id = "42")),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = { id -> editClickedId = id },
                onDeleteClick = {},
                onRetry = {}
            )
        }

        onNodeWithContentDescription("Edit Posting").performClick()

        assertEquals("42", editClickedId)
    }

    @Test
    fun loadingState_showsProgressIndicator() = runComposeUiTest {
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Loading,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = {},
                onRetry = {}
            )
        }
        onNodeWithTag("loading").assertIsDisplayed()
    }

    @Test
    fun deleteConfirmed_emitsDeletedEvent() = runComposeUiTest {
        val repository = FakePostingRepository().apply { seed(posting()) }
        val viewModel = PostingDetailsViewModel(
            GetPostingUseCase(repository),
            DeletePostingUseCase(repository),
            postingId = "1",
        )
        var deleted = false

        setContent {
            PostingDetailsScreen(
                onNavigateBack = {},
                onEditClick = {},
                onDeleted = { deleted = true },
                viewModel = viewModel,
            )
        }

        waitUntilExactlyOneExists(hasText("Groceries"))
        onNodeWithContentDescription("Delete Posting").performClick()
        onNodeWithText("Delete").performClick()

        waitUntil { deleted }
        assertEquals(listOf(posting()), repository.deletedPostings)
    }

    @Test
    fun deleteFailure_showsSnackbar() = runComposeUiTest {
        val repository = FakePostingRepository().apply { seed(posting()) }
        val viewModel = PostingDetailsViewModel(
            GetPostingUseCase(repository),
            DeletePostingUseCase(repository),
            postingId = "1",
        )
        var deleted = false

        setContent {
            PostingDetailsScreen(
                onNavigateBack = {},
                onEditClick = {},
                onDeleted = { deleted = true },
                viewModel = viewModel,
            )
        }

        waitUntilExactlyOneExists(hasText("Groceries"))
        repository.failNextWrite = true
        onNodeWithContentDescription("Delete Posting").performClick()
        onNodeWithText("Delete").performClick()

        waitUntilExactlyOneExists(hasText("Failed to delete. Please try again."))
        assertFalse(deleted, "a failed delete must not report the posting as deleted")
    }
}