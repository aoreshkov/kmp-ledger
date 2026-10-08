package app.oreshkov.ledger.feature.settings.impl

import app.oreshkov.ledger.core.domain.GetThemeModeUseCase
import app.oreshkov.ledger.core.domain.SetThemeModeUseCase
import app.oreshkov.ledger.core.model.settings.ThemeMode
import app.oreshkov.ledger.core.test.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repo = FakeSettingsRepository()
    private val getThemeModeUseCase = GetThemeModeUseCase(repo)
    private val setThemeModeUseCase = SetThemeModeUseCase(repo)

    @BeforeTest fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun uiState_reflectsStoredThemeMode() = runTest {
        repo.setThemeMode(ThemeMode.DARK)
        val vm = SettingsViewModel(getThemeModeUseCase, setThemeModeUseCase)

        val state = vm.uiState.first { it.themeMode == ThemeMode.DARK }

        assertEquals(ThemeMode.DARK, state.themeMode)
    }

    @Test
    fun onThemeModeChange_persistsSelection() = runTest {
        val vm = SettingsViewModel(getThemeModeUseCase, setThemeModeUseCase)

        vm.onThemeModeChange(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, repo.themeMode().first())
    }

    @Test
    fun onSaveErrorShown_clearsSaveError_withoutAThemeChange() = runTest {
        val vm = SettingsViewModel(getThemeModeUseCase, setThemeModeUseCase)
        // Keep the WhileSubscribed stateIn active so uiState.value tracks the sources.
        backgroundScope.launch(testDispatcher) { vm.uiState.collect {} }
        repo.failNextWrite = true
        vm.onThemeModeChange(ThemeMode.DARK)
        assertTrue(vm.uiState.value.saveError)

        vm.onSaveErrorShown()

        assertFalse(vm.uiState.value.saveError)
        assertEquals(ThemeMode.SYSTEM, vm.uiState.value.themeMode)
    }
}
