package app.oreshkov.ledger.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.oreshkov.ledger.core.datastore.createPreferencesDataStore
import app.oreshkov.ledger.core.datastore.dataStoreFileName
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.io.File

@Module
actual class PlatformDataStoreModule {

    @Single
    fun provideDataStore(): DataStore<Preferences> =
        createPreferencesDataStore { jvmDataStoreFile().absolutePath }
}

private fun jvmDataStoreFile(): File {
    val appDataDir: File = when {
        System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true) -> {
            File(System.getProperty("user.home"), "Library/Application Support/ledger")
        }
        System.getProperty("os.name").orEmpty().contains("Windows", ignoreCase = true) -> {
            File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "ledger")
        }
        else -> {
            val xdgData = System.getenv("XDG_DATA_HOME")
            if (!xdgData.isNullOrBlank()) File(xdgData, "ledger")
            else File(System.getProperty("user.home"), ".local/share/ledger")
        }
    }
    check(appDataDir.exists() || appDataDir.mkdirs()) {
        "Failed to create app data directory: ${appDataDir.absolutePath}"
    }
    return File(appDataDir, dataStoreFileName)
}
