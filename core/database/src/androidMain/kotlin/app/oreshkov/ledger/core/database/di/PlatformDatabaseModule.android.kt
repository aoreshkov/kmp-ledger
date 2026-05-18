package app.oreshkov.ledger.core.database.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import app.oreshkov.ledger.core.database.LedgerDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
actual class PlatformDatabaseModule {

    @Single
    fun provideRoomBuilder(@Provided context: Context): RoomDatabase.Builder<LedgerDatabase> {
        val dbFile = context.getDatabasePath("ledger.db")
        return Room.databaseBuilder<LedgerDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
    }
}