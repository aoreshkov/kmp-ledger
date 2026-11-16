package app.oreshkov.ledger

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device proof of the Android half of deep linking, which the host tests cannot see: the
 * manifest's intent filter, `launchMode="singleTop"`, and [MainActivity] reading the link from
 * `onCreate` and `onNewIntent`. What a link resolves to, and what it does to the back stacks,
 * is covered on the host (`BootstrapDeepLinksTest`, `NavigatorTest`, `DesktopUiTest`).
 *
 * The intents name only the package, never the activity, so they resolve through the intent
 * filter as a link tapped in another app would.
 */
@RunWith(AndroidJUnit4::class)
class DeepLinkTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private fun viewIntent(link: String): Intent {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return Intent(Intent.ACTION_VIEW, Uri.parse(link)).setPackage(context.packageName)
    }

    @Test
    fun coldStartFromALink_opensItsScreen_andBackWalksTheSyntheticStack() {
        ActivityScenario.launch<MainActivity>(viewIntent("ledger://postings/new")).use {
            // "Narrative" is the add screen's field; the list beneath it is the synthetic parent.
            composeTestRule.onNodeWithText("Narrative").assertExists()

            composeTestRule.onNodeWithContentDescription("Back").performClick()
            composeTestRule.onNodeWithText("My Postings").assertExists()
        }
    }

    @Test
    fun linkWhileRunning_isDeliveredToTheSameActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            composeTestRule.onNodeWithText("My Postings").assertExists()

            var running: MainActivity? = null
            scenario.onActivity { activity ->
                running = activity
                activity.startActivity(viewIntent("ledger://settings"))
            }
            composeTestRule.onNodeWithText("System default").assertExists()

            // singleTop: the link reached onNewIntent on the running activity. A second
            // instance stacked above it would show the settings screen as well, so the
            // screen alone does not prove it.
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val resumed = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED)
                check(resumed.singleOrNull() === running) { "resumed activities: $resumed" }
            }
        }
    }

    @Test
    fun linkNothingClaims_opensTheAppOnItsStartScreen() {
        ActivityScenario.launch<MainActivity>(viewIntent("ledger://no-such-screen")).use {
            composeTestRule.onNodeWithText("My Postings").assertExists()
        }
    }
}
