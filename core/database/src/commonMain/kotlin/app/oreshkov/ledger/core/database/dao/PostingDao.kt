package app.oreshkov.ledger.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import app.oreshkov.ledger.core.database.model.PostingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostingDao {
    @Insert
    suspend fun insert(posting: PostingEntity)

    @Update
    suspend fun update(posting: PostingEntity)

    /** Soft-delete: tombstone the row so the deletion can propagate to other replicas. */
    @Query("UPDATE postings SET isDeleted = 1, pendingSync = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: String, updatedAt: Long)

    /** Hard-delete: drops a row outright, tombstone included. */
    @Query("DELETE FROM postings WHERE id = :id")
    suspend fun hardDeleteById(id: String)

    /** Live postings only — tombstones never surface to the UI. */
    @Query("SELECT * FROM postings WHERE isDeleted = 0")
    fun getAllPostings(): Flow<List<PostingEntity>>

    @Query("SELECT * FROM postings WHERE id = :id AND isDeleted = 0")
    fun getPostingById(id: String): Flow<PostingEntity?>

    // --- Replication-facing queries ---
    //
    // The app itself never calls these: it reads and writes through the four operations above.
    // They exist so an optional replication layer can be added on top of this DAO without
    // reopening the entity or the schema. Kept here, and covered by tests, so that the
    // last-write-wins contract lives in one place rather than being reinvented alongside it.

    /** Every locally-changed row (including tombstones) awaiting a push. */
    @Query("SELECT * FROM postings WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<PostingEntity>

    /** Snapshot of specific rows (including tombstones) — used to resolve conflicts. */
    @Query("SELECT * FROM postings WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<PostingEntity>

    /**
     * Authoritative apply: overwrite local rows regardless of primary-key clash.
     * Uses INSERT OR REPLACE (rather than @Upsert's ON CONFLICT DO UPDATE) so it also
     * runs on older SQLite builds (e.g. Robolectric) — the table has no foreign keys.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(postings: List<PostingEntity>)

    /**
     * Clears the pending flag for a pushed row, but only if it hasn't changed since it was read
     * for the push (same [updatedAt]). If a concurrent local edit bumped [updatedAt] mid-push, the
     * flag is left set so the newer change is pushed next time instead of being lost.
     */
    @Query("UPDATE postings SET pendingSync = 0 WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun clearPendingSyncIfUnchanged(id: String, updatedAt: Long)
}
