package app.oreshkov.ledger.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import app.oreshkov.ledger.core.database.dao.PostingDao
import app.oreshkov.ledger.core.database.model.PostingEntity

@Database(entities = [PostingEntity::class], version = 1)
@ConstructedBy(LedgerDatabaseConstructor::class)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun postingDao(): PostingDao
}

expect object LedgerDatabaseConstructor : RoomDatabaseConstructor<LedgerDatabase> {
    @Suppress("NO_ACTUAL_FOR_EXPECT")
    override fun initialize(): LedgerDatabase
}