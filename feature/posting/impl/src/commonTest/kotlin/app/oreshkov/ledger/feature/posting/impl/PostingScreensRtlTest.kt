package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.width
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import app.oreshkov.ledger.core.test.posting
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The right-to-left pass over the posting screens. Compose mirrors start/end paddings,
 * alignment and `AutoMirrored` icons by itself; what these tests pin is that the screens leave
 * it to do so, with nothing positioned by `left`/`right` or forced into one direction.
 *
 * The layout direction is set directly rather than through an RTL locale, so the pass needs no
 * translation and reads the same English strings as the other screen tests.
 */
@OptIn(ExperimentalTestApi::class)
class PostingScreensRtlTest : PlatformComposeUiTest() {

    private fun ComposeUiTest.setContentIn(direction: LayoutDirection, content: @Composable () -> Unit) {
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
        }
    }

    /** Whether the node's centre lies in the right half of the screen. */
    private fun ComposeUiTest.isOnTheRight(node: SemanticsNodeInteraction): Boolean {
        val bounds = node.getBoundsInRoot()
        return (bounds.left + bounds.right) / 2 > onRoot().getBoundsInRoot().width / 2
    }

    @Composable
    private fun ListScreen() {
        PostingListContent(
            uiState = PostingListUiState.Empty,
            onAddClick = {},
            onPostingClick = {},
            onRetry = {},
        )
    }

    @Test
    fun list_rightToLeft_startsAtTheRight_withTheAddButtonAtTheLeft() = runComposeUiTest {
        setContentIn(LayoutDirection.Rtl) { ListScreen() }

        assertTrue(isOnTheRight(onNodeWithText("My Postings")))
        assertFalse(isOnTheRight(onNodeWithContentDescription("Add Posting")))
    }

    @Test
    fun list_leftToRight_isTheMirrorImage() = runComposeUiTest {
        // The control: without it, a layout that ignored the direction entirely could still
        // satisfy the right-to-left assertions above by accident.
        setContentIn(LayoutDirection.Ltr) { ListScreen() }

        assertFalse(isOnTheRight(onNodeWithText("My Postings")))
        assertTrue(isOnTheRight(onNodeWithContentDescription("Add Posting")))
    }

    @Test
    fun details_rightToLeft_putsBackAtTheRight_andTheActionsAtTheLeft() = runComposeUiTest {
        setContentIn(LayoutDirection.Rtl) {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting()),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = {},
                onRetry = {},
            )
        }

        assertTrue(isOnTheRight(onNodeWithContentDescription("Back")))
        assertTrue(isOnTheRight(onNodeWithText("Posting Details")))
        assertTrue(isOnTheRight(onNodeWithText("Groceries")))
        assertFalse(isOnTheRight(onNodeWithContentDescription("Delete Posting")))
        assertFalse(isOnTheRight(onNodeWithContentDescription("Edit Posting")))
    }

    @Test
    fun edit_rightToLeft_putsBackAtTheRight_andSaveAtTheLeft() = runComposeUiTest {
        setContentIn(LayoutDirection.Rtl) {
            PostingEditContent(
                uiState = PostingEditUiState.Editing(narrative = "Groceries"),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onNarrativeChange = {},
                onSaveClick = {},
                onRetry = {},
            )
        }

        assertTrue(isOnTheRight(onNodeWithContentDescription("Back")))
        assertTrue(isOnTheRight(onNodeWithText("Add Posting")))
        assertFalse(isOnTheRight(onNodeWithText("Save")))
    }
}
