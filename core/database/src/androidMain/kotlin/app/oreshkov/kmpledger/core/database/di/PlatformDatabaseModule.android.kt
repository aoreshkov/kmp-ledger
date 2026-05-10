package app.oreshkov.kmpledger.core.database.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import app.oreshkov.kmpledger.core.database.KMPLedgerDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformDatabaseModule {

    @Single
    fun provideRoomBuilder(context: Context): RoomDatabase.Builder<KMPLedgerDatabase> {
        val dbFile = context.getDatabasePath("kmpledger.db")
        return Room.databaseBuilder<KMPLedgerDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
    }
}