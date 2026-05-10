package app.oreshkov.kmpledger

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launches_and_displays_initial_screen() {
        // "My Postings" is the title of the PostingListScreen
        composeTestRule.onNodeWithText("My Postings").assertExists()
    }
}