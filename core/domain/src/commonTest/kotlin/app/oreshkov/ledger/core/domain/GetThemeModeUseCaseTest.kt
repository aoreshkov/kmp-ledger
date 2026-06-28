package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.model.settings.ThemeMode
import app.oreshkov.ledger.core.test.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetThemeModeUseCaseTest {

    @Test
    fun `invoke emits current theme mode`() = runTest {
        val repo = FakeSettingsRepository(initial = ThemeMode.DARK)
        val useCase = GetThemeModeUseCase(repo)

        assertEquals(ThemeMode.DARK, useCase().first())
    }

    @Test
    fun `invoke reflects later updates`() = runTest {
        val repo = FakeSettingsRepository()
        val useCase = GetThemeModeUseCase(repo)

        repo.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, useCase().first())
    }
}
