package app.oreshkov.ledger.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.oreshkov.ledger.core.datastore.createPreferencesDataStore
import app.oreshkov.ledger.core.datastore.dataStoreFileName
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
actual class PlatformDataStoreModule {

    @Single
    fun provideDataStore(@Provided context: Context): DataStore<Preferences> =
        createPreferencesDataStore {
            context.applicationContext.filesDir.resolve(dataStoreFileName).absolutePath
        }
}
