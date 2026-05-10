package app.oreshkov.kmpledger.core.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "postings")
data class PostingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Long,
    val timestamp: Instant,
    val currency: String,
    val narrative: String,
)