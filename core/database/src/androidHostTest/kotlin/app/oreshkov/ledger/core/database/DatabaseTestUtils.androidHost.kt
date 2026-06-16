package app.oreshkov.ledger.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createTestDatabase(): LedgerDatabase {
    // Room 3's inMemoryDatabaseBuilder for JVM doesn't require a Context,
    // which simplifies database testing on the Android host (local JVM).
    return Room.inMemoryDatabaseBuilder<LedgerDatabase>()
        .setDriver(BundledSQLiteDriver())
        .build()
}