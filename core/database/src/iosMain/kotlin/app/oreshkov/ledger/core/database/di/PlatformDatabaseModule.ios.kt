package app.oreshkov.ledger.core.database.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import app.oreshkov.ledger.core.database.LedgerDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Single
import org.koin.core.annotation.Module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
actual class PlatformDatabaseModule {

    @Single
    fun provideRoomBuilder(): RoomDatabase.Builder<LedgerDatabase> =
        Room.databaseBuilder<LedgerDatabase>(name = iosDatabaseDirectory() + "/ledger.db" )

    @OptIn(ExperimentalForeignApi::class)
    private fun iosDatabaseDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }
}