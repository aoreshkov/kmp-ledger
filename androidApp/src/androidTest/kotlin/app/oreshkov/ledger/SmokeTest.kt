package app.oreshkov.ledger

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device smoke test: proves the real shipped APK actually runs — real [LedgerApp]/Koin
 * startup, real platform Room DB, real [MainActivity] → App() → Navigation3 graph.
 *
 * Detailed per-screen behavior is covered faster on the host/common UI tests
 * (feature:posting:impl), and the list→add navigation graph is covered on the JVM by
 * desktopApp's DesktopUiTest. This single launch is intentionally thin.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launchesAndNavigatesToAddScreen() {
        // Launch proof: "My Postings" is the PostingListScreen title.
        composeTestRule.onNodeWithText("My Postings").assertExists()

        // One real end-to-end navigation through the live nav graph.
        composeTestRule.onNodeWithContentDescription("Add Posting").performClick()
        composeTestRule.onNodeWithText("Add Posting").assertExists()
    }
}
