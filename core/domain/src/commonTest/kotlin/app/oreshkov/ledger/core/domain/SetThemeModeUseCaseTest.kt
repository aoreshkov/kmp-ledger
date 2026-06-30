package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.model.settings.ThemeMode
import app.oreshkov.ledger.core.test.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetThemeModeUseCaseTest {

    @Test
    fun `invoke persists the selected theme mode`() = runTest {
        val repo = FakeSettingsRepository()
        val useCase = SetThemeModeUseCase(repo)

        val result = useCase(ThemeMode.DARK)

        assertTrue(result.isSuccess)
        assertEquals(ThemeMode.DARK, repo.themeMode().first())
    }

    @Test
    fun `invoke returns failure when the write throws`() = runTest {
        val repo = FakeSettingsRepository().apply { failNextWrite = true }
        val useCase = SetThemeModeUseCase(repo)

        val result = useCase(ThemeMode.DARK)

        assertTrue(result.isFailure)
    }
}
