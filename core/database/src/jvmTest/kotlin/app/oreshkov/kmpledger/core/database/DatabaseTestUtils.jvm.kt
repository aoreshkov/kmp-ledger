package app.oreshkov.kmpledger.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createTestDatabase(): KMPLedgerDatabase {
    return Room.inMemoryDatabaseBuilder<KMPLedgerDatabase>()
        .setDriver(BundledSQLiteDriver())
        .build()
}