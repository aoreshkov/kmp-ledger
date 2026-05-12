package app.oreshkov.ledger.core.database.util

import androidx.room3.TypeConverter
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = 
        value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? = 
        instant?.toEpochMilliseconds()
}