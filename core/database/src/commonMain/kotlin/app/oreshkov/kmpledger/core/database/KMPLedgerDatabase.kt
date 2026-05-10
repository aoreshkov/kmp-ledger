package app.oreshkov.kmpledger.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.TypeConverters
import app.oreshkov.kmpledger.core.database.dao.PostingDao
import app.oreshkov.kmpledger.core.database.model.PostingEntity
import app.oreshkov.kmpledger.core.database.util.Converters

@Database(entities = [PostingEntity::class], version = 1)
@TypeConverters(Converters::class)
@ConstructedBy(KMPLedgerDatabaseConstructor::class)
abstract class KMPLedgerDatabase : RoomDatabase() {
    abstract fun postingDao(): PostingDao
}

expect object KMPLedgerDatabaseConstructor : RoomDatabaseConstructor<KMPLedgerDatabase> {
    override fun initialize(): KMPLedgerDatabase
}