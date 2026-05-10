package app.oreshkov.kmpledger.core.database.di

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.oreshkov.kmpledger.core.database.KMPLedgerDatabase
import app.oreshkov.kmpledger.core.database.dao.PostingDao
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [PlatformDatabaseModule::class])
class DatabaseModule {

    @Single
    fun provideDatabase(
        builder: RoomDatabase.Builder<KMPLedgerDatabase>
    ): KMPLedgerDatabase =
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    @Single
    fun providePostingDao(db: KMPLedgerDatabase): PostingDao = db.postingDao()
}