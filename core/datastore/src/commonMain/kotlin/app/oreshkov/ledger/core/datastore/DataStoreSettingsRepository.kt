package app.oreshkov.ledger.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.oreshkov.ledger.core.domain.repository.SettingsRepository
import app.oreshkov.ledger.core.model.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException

internal class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun themeMode(): Flow<ThemeMode> = dataStore.data
        .catch { exception ->
            // DataStore guidance: recover from read IO errors by emitting empty prefs.
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[themeModeKey].toThemeMode() }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences -> preferences[themeModeKey] = mode.name }
    }

    // Tolerates a missing or unrecognised stored value by falling back to SYSTEM.
    private fun String?.toThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.SYSTEM

    private companion object {
        val themeModeKey = stringPreferencesKey("theme_mode")
    }
}
