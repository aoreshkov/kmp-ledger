package app.oreshkov.ledger

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigate_from_list_to_add_screen() {
        // The strings from commonMain resources are resolved in the app.
        // In the test, we match against the actual displayed text.
        
        composeTestRule.onNodeWithContentDescription("Add Posting").performClick()

        // Verify we are on the "Add Posting" screen
        composeTestRule.onNodeWithText("Add Posting").assertExists()
    }
}