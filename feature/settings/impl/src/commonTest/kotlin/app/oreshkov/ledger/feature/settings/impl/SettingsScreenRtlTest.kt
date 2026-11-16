package app.oreshkov.ledger.feature.settings.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import app.oreshkov.ledger.core.model.settings.ThemeMode
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The right-to-left pass over the settings screen. The layout direction is set directly rather
 * than through an RTL locale, so the pass needs no translation. See `PostingScreensRtlTest` for
 * what the pass is meant to catch.
 */
@OptIn(ExperimentalTestApi::class)
class SettingsScreenRtlTest : PlatformComposeUiTest() {

    private fun ComposeUiTest.setContentIn(direction: LayoutDirection) {
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                SettingsContent(
                    uiState = SettingsUiState(ThemeMode.SYSTEM),
                    snackbarHostState = remember { SnackbarHostState() },
                    onNavigateBack = {},
                    onThemeModeChange = {},
                )
            }
        }
    }

    @Test
    fun rightToLeft_putsBackAtTheRight_andEachRadioButtonRightOfItsLabel() = runComposeUiTest {
        setContentIn(LayoutDirection.Rtl)

        val screenWidth = onRoot().getBoundsInRoot().width
        val back = onNodeWithContentDescription("Back").getBoundsInRoot()
        assertTrue(back.left > screenWidth / 2, "back arrow at $back of $screenWidth")

        // The label is a child merged into its row; the radio button takes the row's start edge.
        val row = onNodeWithTag("theme_light").getBoundsInRoot()
        val label = onNodeWithText("Light", useUnmergedTree = true).getBoundsInRoot()
        assertTrue(row.right - label.right >= RADIO_BUTTON_SPACE, "label $label in row $row")
    }

    @Test
    fun leftToRight_isTheMirrorImage() = runComposeUiTest {
        // The control: a layout that ignored the direction could otherwise pass the test above.
        setContentIn(LayoutDirection.Ltr)

        val screenWidth = onRoot().getBoundsInRoot().width
        val back = onNodeWithContentDescription("Back").getBoundsInRoot()
        assertTrue(back.right < screenWidth / 2, "back arrow at $back of $screenWidth")

        val row = onNodeWithTag("theme_light").getBoundsInRoot()
        val label = onNodeWithText("Light", useUnmergedTree = true).getBoundsInRoot()
        assertTrue(label.left - row.left >= RADIO_BUTTON_SPACE, "label $label in row $row")
    }

    private companion object {
        /** The radio button (20dp) plus the label's 16dp start padding, with room to spare. */
        val RADIO_BUTTON_SPACE = 30.dp
    }
}
