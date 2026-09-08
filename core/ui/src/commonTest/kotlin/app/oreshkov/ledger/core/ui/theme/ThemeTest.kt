package app.oreshkov.ledger.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.model.settings.ThemeMode
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ThemeTest : PlatformComposeUiTest() {

    @Test
    fun lightMode_appliesTheLightColorScheme() = runComposeUiTest {
        var primary = Color.Unspecified

        setContent {
            LedgerTheme(ThemeMode.LIGHT) { primary = MaterialTheme.colorScheme.primary }
        }

        assertEquals(Purple40, primary)
    }

    @Test
    fun darkMode_appliesTheDarkColorScheme() = runComposeUiTest {
        var primary = Color.Unspecified

        setContent {
            LedgerTheme(ThemeMode.DARK) { primary = MaterialTheme.colorScheme.primary }
        }

        assertEquals(Purple80, primary)
    }

    @Test
    fun systemMode_defersToTheOsSetting() = runComposeUiTest {
        var fromSystemMode = Color.Unspecified
        var fromDefaultOverload = Color.Unspecified

        setContent {
            LedgerTheme(ThemeMode.SYSTEM) { fromSystemMode = MaterialTheme.colorScheme.primary }
            // The no-argument overload defaults to `isSystemInDarkTheme()`, which is exactly what
            // ThemeMode.SYSTEM is specified to defer to. Comparing the two asserts the deferral
            // without pinning the host's dark-mode setting.
            LedgerTheme { fromDefaultOverload = MaterialTheme.colorScheme.primary }
        }

        assertEquals(fromDefaultOverload, fromSystemMode)
        assertTrue(
            fromSystemMode == Purple40 || fromSystemMode == Purple80,
            "unexpected scheme: $fromSystemMode",
        )
    }
}
