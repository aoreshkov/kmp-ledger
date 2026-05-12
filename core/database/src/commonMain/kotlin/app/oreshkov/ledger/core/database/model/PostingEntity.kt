package app.oreshkov.ledger.core.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "postings")
data class PostingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val narrative: String,
)