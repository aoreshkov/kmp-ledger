package app.oreshkov.ledger.core.database.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import app.oreshkov.ledger.core.database.LedgerDatabase
import org.koin.core.annotation.Single
import org.koin.core.annotation.Module
import java.io.File

@Module
actual class PlatformDatabaseModule {

    @Single
    fun provideRoomBuilder(): RoomDatabase.Builder<LedgerDatabase> =
        Room.databaseBuilder<LedgerDatabase>(name = jvmDatabaseFile().absolutePath)
}

private fun jvmDatabaseFile(): File {
    val appDataDir: File = when {
        System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true) -> {
            File(System.getProperty("user.home"), "Library/Application Support/ledger")
        }
        System.getProperty("os.name").orEmpty().contains("Windows", ignoreCase = true) -> {
            File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "ledger")
        }
        else -> {
            val xdgData = System.getenv("XDG_DATA_HOME")
            if (!xdgData.isNullOrBlank()) File(xdgData, "ledger")
            else File(System.getProperty("user.home"), ".local/share/ledger")
        }
    }
    check(appDataDir.exists() || appDataDir.mkdirs()) {
        "Failed to create app data directory: ${appDataDir.absolutePath}"
    }
    return File(appDataDir, "ledger.db")
}