package app.oreshkov.ledger.core.database.di

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.oreshkov.ledger.core.database.LedgerDatabase
import app.oreshkov.ledger.core.database.dao.PostingDao
import kotlinx.coroutines.Dispatchers
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
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()

    @Single
    fun providePostingDao(db: LedgerDatabase): PostingDao = db.postingDao()
}