package app.oreshkov.ledger.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.oreshkov.ledger.core.model.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataStoreSettingsRepositoryTest {

    private val tempDir: File = Files.createTempDirectory("datastore-test").toFile()

    private val dataStoreFile = File(tempDir, "ledger.preferences_pb")

    @AfterTest
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    // A single DataStore instance per file; DataStore forbids two active instances on one file.
    private fun newDataStore() = createPreferencesDataStore { dataStoreFile.absolutePath }

    private fun dataStoreOf(
        data: Flow<Preferences>,
        update: suspend () -> Preferences = { error("not used") },
    ) = object : DataStore<Preferences> {
        override val data: Flow<Preferences> = data
        override suspend fun updateData(
            transform: suspend (Preferences) -> Preferences
        ): Preferences = update()
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

    @Test
    fun `recovers from read IO error by falling back to SYSTEM`() = runTest {
        val repo = DataStoreSettingsRepository(dataStoreOf(flow { throw okio.IOException("boom") }))

        assertEquals(ThemeMode.SYSTEM, repo.themeMode().first())
    }

    @Test
    fun `corrupt file is replaced and stays writable`() = runTest {
        dataStoreFile.writeText("not a protobuf")
        val repo = DataStoreSettingsRepository(newDataStore())

        assertEquals(ThemeMode.SYSTEM, repo.themeMode().first())
        // Reads alone can't tell the handler ran: the repository maps CorruptionException
        // (an IOException) to SYSTEM too. Without ReplaceFileCorruptionHandler the file stays
        // corrupt and this write throws, because updateData has to read the current value first.
        repo.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repo.themeMode().first())
    }

    @Test
    fun `non-IO read failure is rethrown, not masked as SYSTEM`() = runTest {
        val repo = DataStoreSettingsRepository(dataStoreOf(flow { throw IllegalStateException("boom") }))

        val thrown = assertFailsWith<IllegalStateException> { repo.themeMode().first() }
        assertEquals("boom", thrown.message)
    }

    @Test
    fun `write failure propagates to the caller`() = runTest {
        // SettingsViewModel turns this into its save-error snackbar, so it must not be swallowed.
        val repo = DataStoreSettingsRepository(
            dataStoreOf(data = flow { error("not used") }, update = { throw okio.IOException("disk full") }),
        )

        val thrown = assertFailsWith<okio.IOException> { repo.setThemeMode(ThemeMode.DARK) }
        assertEquals("disk full", thrown.message)
    }
}
