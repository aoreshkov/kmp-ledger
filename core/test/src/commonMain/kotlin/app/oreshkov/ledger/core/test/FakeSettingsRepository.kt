package app.oreshkov.ledger.core.test

import app.oreshkov.ledger.core.domain.repository.SettingsRepository
import app.oreshkov.ledger.core.model.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository(
    initial: ThemeMode = ThemeMode.SYSTEM
) : SettingsRepository {

    private val _themeMode = MutableStateFlow(initial)

    override fun themeMode(): Flow<ThemeMode> = _themeMode.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
}
