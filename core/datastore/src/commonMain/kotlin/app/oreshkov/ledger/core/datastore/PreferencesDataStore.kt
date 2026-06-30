package app.oreshkov.ledger.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import okio.Path.Companion.toPath

internal const val dataStoreFileName = "ledger.preferences_pb"

/**
 * Shared Preferences-DataStore factory. Each platform supplies the absolute file path
 * for [dataStoreFileName] via its `PlatformDataStoreModule` actual.
 */
internal fun createPreferencesDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { producePath().toPath() },
    )
