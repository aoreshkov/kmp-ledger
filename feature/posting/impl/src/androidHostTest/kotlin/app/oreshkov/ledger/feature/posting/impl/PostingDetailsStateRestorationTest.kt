package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import app.oreshkov.ledger.core.test.posting
import kotlin.test.Test

/**
 * An open delete confirmation must survive Activity recreation (rotation), as
 * `MainActivity` declares no `configChanges`.
 *
 * **Android-only on purpose.** [StateRestorationTester] is a `TODO` stub on the skiko
 * targets (CMP-6836); see `AppStateRestorationTest` in `core:ui`.
 */
@OptIn(ExperimentalTestApi::class)
class PostingDetailsStateRestorationTest : PlatformComposeUiTest() {

    @Test
    fun openDeleteDialog_survivesSaveAndRestore() = runComposeUiTest {
        val tester = StateRestorationTester(this)
        tester.setContent {
            PostingDetailsContent(
                uiState = PostingDetailsUiState.Success(posting()),
                snackbarHostState = SnackbarHostState(),
                onNavigateBack = {},
                onEditClick = {},
                onDeleteClick = {},
                onRetry = {}
            )
        }
        onNodeWithContentDescription("Delete Posting").performClick()
        onNodeWithText("Delete posting?").assertIsDisplayed()

        tester.emulateSaveAndRestore()

        onNodeWithText("Delete posting?").assertIsDisplayed()
    }
}
