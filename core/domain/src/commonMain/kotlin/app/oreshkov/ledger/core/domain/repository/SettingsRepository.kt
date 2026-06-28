package app.oreshkov.ledger.core.domain.repository

import app.oreshkov.ledger.core.model.settings.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun themeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
