package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import app.oreshkov.ledger.core.test.posting
import kotlin.test.Test

/**
 * What a screen reader is told about the posting screens, beyond the visible text: each screen
 * title is a heading (the landmark "jump to next heading" navigates by), and a labelled value
 * is announced as one item rather than two unrelated ones.
 */
@OptIn(ExperimentalTestApi::class)
class PostingScreensAccessibilityTest : PlatformComposeUiTest() {

    private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    @Test
    fun list_titleIsAHeading() = runComposeUiTest {
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Empty,
                onAddClick = {},
                onPostingClick = {},
                onRetry = {},
            )
        }

        onNodeWithText("My Postings").assert(isHeading)
        // The control: the property is on the title, not on every text in the screen.
        onNodeWithText("No postings added yet.").assert(!isHeading)
    }

    @Test
    fun details_titleIsAHeading_andTheFieldIsAnnouncedWithItsLabel() = runComposeUiTest {
        setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting()),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = {},
                onRetry = {},
            )
        }

        onNodeWithText("Posting Details").assert(isHeading)
        // One merged node carrying both texts: "Narrative, Groceries".
        onNodeWithText("Groceries").assert(hasText("Narrative"))
    }

    @Test
    fun edit_titleIsAHeading() = runComposeUiTest {
        setContent {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(narrative = "Groceries"),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {},
            )
        }

        onNodeWithText("Add Posting").assert(isHeading)
    }
}
