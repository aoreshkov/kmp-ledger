package app.oreshkov.ledger.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.oreshkov.ledger.core.datastore.DataStoreSettingsRepository
import app.oreshkov.ledger.core.domain.repository.SettingsRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [PlatformDataStoreModule::class])
class DataStoreModule {
    @Single
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>
    ): SettingsRepository = DataStoreSettingsRepository(dataStore)
}
