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

    var failNextWrite: Boolean = false

    override fun themeMode(): Flow<ThemeMode> = _themeMode.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        if (failNextWrite) { failNextWrite = false; error("settings write error") }
        _themeMode.value = mode
    }
}
