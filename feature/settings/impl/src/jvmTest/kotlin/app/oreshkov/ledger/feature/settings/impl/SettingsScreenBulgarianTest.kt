package app.oreshkov.ledger.feature.settings.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.model.settings.ThemeMode
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Renders the screen under the Bulgarian locale: proof that the `values-bg` resources are
 * packaged and picked, here and in `core:compose`, whose "Back" label this screen borrows.
 * Key-for-key completeness is the `checkStringResourceParity` task's job, not this test's.
 *
 * JVM only, because the default locale is switched through `java.util.Locale`.
 */
@OptIn(ExperimentalTestApi::class)
class SettingsScreenBulgarianTest {

    private val originalLocale: Locale = Locale.getDefault()

    @BeforeTest fun useBulgarian() { Locale.setDefault(Locale.forLanguageTag("bg")) }
    @AfterTest fun restoreLocale() { Locale.setDefault(originalLocale) }

    @Test
    fun showsBulgarianStrings() = runComposeUiTest {
        setContent {
            SettingsContent(
                uiState = SettingsUiState(ThemeMode.SYSTEM),
                snackbarHostState = remember { SnackbarHostState() },
                onNavigateBack = {},
                onThemeModeChange = {},
            )
        }

        onNodeWithText("Настройки").assertIsDisplayed()
        onNodeWithText("Тема").assertIsDisplayed()
        onNodeWithText("Стандартна за системата").assertIsDisplayed()
        onNodeWithContentDescription("Назад").assertIsDisplayed()
    }
}
