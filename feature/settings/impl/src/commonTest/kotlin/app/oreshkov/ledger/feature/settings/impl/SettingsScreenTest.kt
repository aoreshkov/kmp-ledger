package app.oreshkov.ledger.feature.settings.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.domain.GetThemeModeUseCase
import app.oreshkov.ledger.core.domain.SetThemeModeUseCase
import app.oreshkov.ledger.core.model.settings.ThemeMode
import app.oreshkov.ledger.core.test.FakeSettingsRepository
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class SettingsScreenTest : PlatformComposeUiTest() {

    // The SettingsScreen wrapper collects the ViewModel flow via collectAsStateWithLifecycle,
    // which dispatches on Dispatchers.Main; coroutines-test leaves Main unset until setMain.
    @BeforeTest fun setUp()    { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun showsAllThemeOptions() = runComposeUiTest {
        setContent {
            SettingsContent(
                uiState = SettingsUiState(ThemeMode.SYSTEM),
                snackbarHostState = remember { SnackbarHostState() },
                onNavigateBack = {},
                onThemeModeChange = {},
            )
        }
        onNodeWithText("Light").assertIsDisplayed()
        onNodeWithText("Dark").assertIsDisplayed()
        onNodeWithText("System default").assertIsDisplayed()
    }

    @Test
    fun marksCurrentThemeModeSelected() = runComposeUiTest {
        setContent {
            SettingsContent(
                uiState = SettingsUiState(ThemeMode.DARK),
                snackbarHostState = remember { SnackbarHostState() },
                onNavigateBack = {},
                onThemeModeChange = {},
            )
        }
        onNodeWithTag("theme_dark").assertIsSelected()
    }

    @Test
    fun clickingOption_triggersOnThemeModeChange() = runComposeUiTest {
        var selected: ThemeMode? = null
        setContent {
            SettingsContent(
                uiState = SettingsUiState(ThemeMode.SYSTEM),
                snackbarHostState = remember { SnackbarHostState() },
                onNavigateBack = {},
                onThemeModeChange = { selected = it },
            )
        }
        onNodeWithTag("theme_light").performClick()
        assertEquals(ThemeMode.LIGHT, selected)
    }

    @Test
    fun clickingBack_triggersOnNavigateBack() = runComposeUiTest {
        var backClicked = false
        setContent {
            SettingsContent(
                uiState = SettingsUiState(ThemeMode.SYSTEM),
                snackbarHostState = remember { SnackbarHostState() },
                onNavigateBack = { backClicked = true },
                onThemeModeChange = {},
            )
        }
        onNodeWithContentDescription("Back").performClick()
        assertEquals(true, backClicked)
    }

    @Test
    fun screen_rendersStoredSelectionAndForwardsChangesToViewModel() = runComposeUiTest {
        val repo = FakeSettingsRepository(initial = ThemeMode.DARK)
        val viewModel = SettingsViewModel(GetThemeModeUseCase(repo), SetThemeModeUseCase(repo))

        setContent {
            SettingsScreen(onNavigateBack = {}, viewModel = viewModel)
        }

        onNodeWithTag("theme_dark").assertIsSelected()

        onNodeWithTag("theme_light").performClick()
        waitForIdle()

        onNodeWithTag("theme_light").assertIsSelected()
    }

    @Test
    fun screen_showsSnackbar_whenWriteFails() = runComposeUiTest {
        val repo = FakeSettingsRepository(initial = ThemeMode.SYSTEM).apply { failNextWrite = true }
        val viewModel = SettingsViewModel(GetThemeModeUseCase(repo), SetThemeModeUseCase(repo))

        setContent {
            SettingsScreen(onNavigateBack = {}, viewModel = viewModel)
        }

        onNodeWithTag("theme_dark").performClick()
        waitForIdle()

        onNodeWithText("Failed to save. Please try again.").assertIsDisplayed()
    }
}
