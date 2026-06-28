package app.oreshkov.ledger.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.oreshkov.ledger.core.model.settings.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DataStoreSettingsRepositoryTest {

    private val tempDir: File = Files.createTempDirectory("datastore-test").toFile()

    @AfterTest
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    // A single DataStore instance per file; DataStore forbids two active instances on one file.
    private fun newDataStore() = createPreferencesDataStore {
        File(tempDir, "ledger.preferences_pb").absolutePath
    }

    @Test
    fun `defaults to SYSTEM when nothing is stored`() = runTest {
        val repo = DataStoreSettingsRepository(newDataStore())

        assertEquals(ThemeMode.SYSTEM, repo.themeMode().first())
    }

    @Test
    fun `set then get round-trips the theme mode`() = runTest {
        val repo = DataStoreSettingsRepository(newDataStore())

        repo.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repo.themeMode().first())
    }

    @Test
    fun `unrecognised stored value falls back to SYSTEM`() = runTest {
        val dataStore = newDataStore()
        dataStore.edit { it[stringPreferencesKey("theme_mode")] = "NONSENSE" }

        val repo = DataStoreSettingsRepository(dataStore)

        assertEquals(ThemeMode.SYSTEM, repo.themeMode().first())
    }
}
