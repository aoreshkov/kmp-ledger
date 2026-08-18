package app.oreshkov.ledger.core.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "postings")
data class PostingEntity(
    @PrimaryKey
    val id: String,
    val narrative: String,
    // Sync metadata — data-layer only, never mapped into the domain `Posting`.
    // `updatedAt`: client write time (epoch millis); the last-write-wins key.
    val updatedAt: Long = 0L,
    // `isDeleted`: soft-delete tombstone so deletions can propagate to other replicas.
    val isDeleted: Boolean = false,
    // `pendingSync`: row changed locally but not yet pushed to a replica.
    val pendingSync: Boolean = false,
)
