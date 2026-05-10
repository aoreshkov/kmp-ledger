package app.oreshkov.kmpledger.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createTestDatabase(): KMPLedgerDatabase {
    // For Android Host Tests (local JVM tests), we can't easily get a Context
    // unless we use Robolectric. 
    // However, Room 3's inMemoryDatabaseBuilder for JVM (non-Android) doesn't require Context.
    // If this is running on the JVM but targeting Android, we might need a specific approach.
    return Room.inMemoryDatabaseBuilder<KMPLedgerDatabase>()
        .setDriver(BundledSQLiteDriver())
        .build()
}