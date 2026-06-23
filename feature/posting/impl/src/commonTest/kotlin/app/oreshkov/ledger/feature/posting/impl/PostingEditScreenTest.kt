package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import app.oreshkov.ledger.core.domain.GetPostingUseCase
import app.oreshkov.ledger.core.domain.SavePostingUseCase
import app.oreshkov.ledger.core.test.FakePostingRepository
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class PostingEditScreenTest : PlatformComposeUiTest() {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validationErrors_areShown_whenFieldsAreBlankAndTouched() = runComposeUiTest {
        val uiState = PostingEditUiState.Editing(
            narrativeTouched = true,
            narrative = ""
        )

        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }

        onNodeWithText("Narrative is required").performScrollTo().assertIsDisplayed()
        onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun narrativeError_notShown_whenUntouched() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(narrative = ""),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Narrative is required").assertDoesNotExist()
    }

    @Test
    fun saveButton_isDisabled_whenNarrativeBlank() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(narrative = ""),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun saveButton_isEnabled_whenNarrativePresent() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(narrative = "Groceries"),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun errorState_showsErrorMessage() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Error,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Failed to load posting details.").assertIsDisplayed()
    }

    @Test
    fun addMode_showsAddTitle() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(isEditMode = false),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Add Posting").assertIsDisplayed()
    }

    @Test
    fun editMode_showsEditTitleAndPopulatesFields() = runComposeUiTest {
        val uiState = PostingEditUiState.Editing(
            isEditMode = true,
            narrative = "Groceries"
        )
        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Edit Posting").assertIsDisplayed()
        onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun typingInFields_triggersCallbacks() = runComposeUiTest {
        var newNarrative = ""
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = { newNarrative = it },
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Narrative").performTextInput("Groceries")
        assertEquals("Groceries", newNarrative)
    }

    @Test
    fun clickingSave_triggersOnSaveClick() = runComposeUiTest {
        var saveClicked = false
        val uiState = PostingEditUiState.Editing(
            narrative = "Groceries"
        )
        setContent {
            PostingEditContent(
                uiState = uiState,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = { saveClicked = true },
                onRetry = {}
            )
        }
        onNodeWithText("Save").performClick()
        assertTrue(saveClicked)
    }

    @Test
    fun notFoundState_showsNotFoundMessageAndGoBackButton() = runComposeUiTest {
        var backClicked = false
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.NotFound,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = { backClicked = true },
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithText("Posting not found.").assertIsDisplayed()
        onNodeWithText("Go back").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun saveError_showsSnackbar() = runComposeUiTest {
        val repo = FakePostingRepository()
        val getPostingUseCase = GetPostingUseCase(repo)
        val savePostingUseCase = SavePostingUseCase(repo)
        val viewModel = PostingEditViewModel(getPostingUseCase, savePostingUseCase, null)
        
        setContent {
             PostingEditScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Simulate save error
        repo.failNextWrite = true
        onNodeWithText("Narrative").performTextInput("Groceries")
        onNodeWithText("Save").performClick()

        // Snackbar should appear (wait for the async LaunchedEffect to surface it).
        waitUntilExactlyOneExists(hasText("Failed to save. Please try again."))
        onNodeWithText("Failed to save. Please try again.").assertIsDisplayed()
    }

    @Test
    fun loadingState_showsProgressIndicator() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Loading,
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {}
            )
        }
        onNodeWithTag("loading").assertIsDisplayed()
    }
}