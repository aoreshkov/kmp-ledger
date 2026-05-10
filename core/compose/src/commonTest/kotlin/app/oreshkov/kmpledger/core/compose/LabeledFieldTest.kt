package app.oreshkov.kmpledger.core.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.kmpledger.core.test.PlatformComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LabeledFieldTest : PlatformComposeUiTest() {

    @Test
    fun labelAndValue_areDisplayed() = runComposeUiTest {
        setContent {
            LabeledField(
                label = "Test Label",
                value = "Test Value"
            )
        }

        onNodeWithText("Test Label").assertIsDisplayed()
        onNodeWithText("Test Value").assertIsDisplayed()
    }

    @Test
    fun customStyle_isApplied() = runComposeUiTest {
        setContent {
            LabeledField(
                label = "Label",
                value = "Large Value",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        onNodeWithText("Label").assertIsDisplayed()
        onNodeWithText("Large Value").assertIsDisplayed()
    }
}