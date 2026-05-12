package app.oreshkov.ledger.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createTestDatabase(): LedgerDatabase {
    return Room.inMemoryDatabaseBuilder<LedgerDatabase>()
        .setDriver(BundledSQLiteDriver())
        .build()
}