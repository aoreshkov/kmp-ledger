package app.oreshkov.ledger.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.oreshkov.ledger.core.datastore.createPreferencesDataStore
import app.oreshkov.ledger.core.datastore.dataStoreFileName
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
actual class PlatformDataStoreModule {

    @Single
    fun provideDataStore(): DataStore<Preferences> =
        createPreferencesDataStore { iosDataStorePath() }

    @OptIn(ExperimentalForeignApi::class)
    private fun iosDataStorePath(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path) + "/$dataStoreFileName"
    }
}
