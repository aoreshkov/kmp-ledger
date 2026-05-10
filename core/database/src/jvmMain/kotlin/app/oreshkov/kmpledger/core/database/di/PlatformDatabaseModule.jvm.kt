package app.oreshkov.kmpledger.core.database.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import app.oreshkov.kmpledger.core.database.KMPLedgerDatabase
import org.koin.core.annotation.Single
import org.koin.core.annotation.Module
import java.io.File

@Module
actual class PlatformDatabaseModule {

    @Single
    fun provideRoomBuilder(): RoomDatabase.Builder<KMPLedgerDatabase> =
        Room.databaseBuilder<KMPLedgerDatabase>(name = jvmDatabaseFile().absolutePath)
}

private fun jvmDatabaseFile(): File {
    val appDataDir: File = when {
        System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true) -> {
            File(System.getProperty("user.home"), "Library/Application Support/kmpledger")
        }
        System.getProperty("os.name").orEmpty().contains("Windows", ignoreCase = true) -> {
            File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "kmpledger")
        }
        else -> {
            val xdgData = System.getenv("XDG_DATA_HOME")
            if (!xdgData.isNullOrBlank()) File(xdgData, "kmpledger")
            else File(System.getProperty("user.home"), ".local/share/kmpledger")
        }
    }
    check(appDataDir.exists() || appDataDir.mkdirs()) {
        "Failed to create app data directory: ${appDataDir.absolutePath}"
    }
    return File(appDataDir, "kmpledger.db")
}