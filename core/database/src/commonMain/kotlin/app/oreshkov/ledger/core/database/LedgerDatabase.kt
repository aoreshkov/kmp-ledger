package app.oreshkov.ledger.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.TypeConverters
import app.oreshkov.ledger.core.database.dao.PostingDao
import app.oreshkov.ledger.core.database.model.PostingEntity
import app.oreshkov.ledger.core.database.util.Converters

@Database(entities = [PostingEntity::class], version = 1)
@TypeConverters(Converters::class)
@ConstructedBy(LedgerDatabaseConstructor::class)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun postingDao(): PostingDao
}

expect object LedgerDatabaseConstructor : RoomDatabaseConstructor<LedgerDatabase>