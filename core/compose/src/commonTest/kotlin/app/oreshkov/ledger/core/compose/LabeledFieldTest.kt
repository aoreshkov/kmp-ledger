package app.oreshkov.ledger.core.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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
        lateinit var custom: TextStyle
        lateinit var default: TextStyle
        setContent {
            custom = MaterialTheme.typography.headlineLarge
            default = MaterialTheme.typography.bodyLarge
            LabeledField(
                label = "Label",
                value = "Large Value",
                style = custom
            )
        }

        // Guard: the two sizes must differ, or a dropped `style` would still match.
        assertNotEquals(default.fontSize, custom.fontSize)
        assertEquals(custom.fontSize, onNodeWithText("Large Value").renderedTextStyle().fontSize)
    }

    /** The style the node was actually laid out with, after merging with LocalTextStyle. */
    private fun SemanticsNodeInteraction.renderedTextStyle(): TextStyle {
        val layouts = mutableListOf<TextLayoutResult>()
        val getLayout = fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)
        checkNotNull(getLayout?.action) { "node exposes no GetTextLayoutResult" }.invoke(layouts)
        return layouts.single().layoutInput.style
    }
}
