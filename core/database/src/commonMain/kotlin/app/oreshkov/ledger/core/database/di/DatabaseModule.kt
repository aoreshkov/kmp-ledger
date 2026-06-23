package app.oreshkov.ledger.core.database.di

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.oreshkov.ledger.core.database.LedgerDatabase
import app.oreshkov.ledger.core.database.dao.PostingDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [PlatformDatabaseModule::class])
class DatabaseModule {

    @Single
    fun provideDatabase(
        builder: RoomDatabase.Builder<LedgerDatabase>
    ): LedgerDatabase =
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            // Pre-release migration posture: with no Migration objects defined, the first
            // `version` bump would otherwise crash at open on every platform. While the app
            // is pre-release and ships no data worth preserving, drop and recreate on any
            // unrecognized schema. Before shipping real user data, replace this with explicit
            // `Migration` objects (and a CI check that the exported schema changed on bump).
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Single
    fun providePostingDao(db: LedgerDatabase): PostingDao = db.postingDao()
}