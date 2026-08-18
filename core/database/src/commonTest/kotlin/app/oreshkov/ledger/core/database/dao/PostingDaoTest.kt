package app.oreshkov.ledger.core.database.dao

import app.oreshkov.ledger.core.database.LedgerDatabase
import app.oreshkov.ledger.core.database.createTestDatabase
import app.oreshkov.ledger.core.database.model.PostingEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PostingDaoTest {

    private lateinit var db: LedgerDatabase
    private lateinit var dao: PostingDao

    @BeforeTest
    fun setUp() {
        db = createTestDatabase()
        dao = db.postingDao()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetPosting() = runTest {
        val posting = PostingEntity(id = "1", narrative = "Groceries")
        dao.insert(posting)

        val loaded = dao.getPostingById("1").first()
        assertEquals(posting, loaded)
    }

    @Test
    fun getAllPostings() = runTest {
        val posting1 = PostingEntity(id = "1", narrative = "Groceries")
        val posting2 = PostingEntity(id = "2", narrative = "Other Groceries")
        dao.insert(posting1)
        dao.insert(posting2)

        val allPostings = dao.getAllPostings().first()
        assertEquals(2, allPostings.size)
        assertEquals(listOf(posting1, posting2), allPostings)
    }

    @Test
    fun updatePosting() = runTest {
        val posting = PostingEntity(id = "1", narrative = "Groceries")
        dao.insert(posting)

        val updatedPosting = posting.copy(narrative = "Groceries updated")
        dao.update(updatedPosting)

        val loaded = dao.getPostingById("1").first()
        assertEquals("Groceries updated", loaded?.narrative)
    }

    @Test
    fun softDeleteTombstonesRowAndHidesItFromReads() = runTest {
        val posting = PostingEntity(id = "1", narrative = "Groceries")
        dao.insert(posting)
        dao.softDeleteById(posting.id, updatedAt = 42L)

        // Hidden from live reads...
        assertNull(dao.getPostingById("1").first())
        assertTrue(dao.getAllPostings().first().isEmpty())

        // ...but still present as a pending tombstone.
        val pending = dao.getPendingSync()
        assertEquals(1, pending.size)
        assertTrue(pending[0].isDeleted)
        assertTrue(pending[0].pendingSync)
        assertEquals(42L, pending[0].updatedAt)
    }

    @Test
    fun hardDeleteRemovesRowEntirely() = runTest {
        val posting = PostingEntity(id = "1", narrative = "Groceries", isDeleted = true)
        dao.insert(posting)
        dao.hardDeleteById(posting.id)

        assertTrue(dao.getPendingSync().isEmpty())
    }

    @Test
    fun getPendingSyncReturnsOnlyDirtyRows() = runTest {
        dao.insert(PostingEntity(id = "1", narrative = "clean", pendingSync = false))
        dao.insert(PostingEntity(id = "2", narrative = "dirty", pendingSync = true))

        val pending = dao.getPendingSync()
        assertEquals(listOf("2"), pending.map { it.id })
    }

    @Test
    fun upsertOverwritesExistingRow() = runTest {
        dao.insert(PostingEntity(id = "1", narrative = "old", pendingSync = true))

        // Authoritative apply overwrites the existing row.
        dao.upsert(listOf(PostingEntity(id = "1", narrative = "new", updatedAt = 99L)))
        assertEquals("new", dao.getPostingById("1").first()?.narrative)
    }

    @Test
    fun clearPendingSyncIfUnchangedOnlyClearsUntouchedRows() = runTest {
        dao.insert(PostingEntity(id = "1", narrative = "pushed", updatedAt = 5L, pendingSync = true))

        // A row re-edited since the push snapshot (updatedAt bumped) keeps its pending flag.
        dao.clearPendingSyncIfUnchanged("1", updatedAt = 4L)
        assertEquals(listOf("1"), dao.getPendingSync().map { it.id })

        // Matching updatedAt clears it.
        dao.clearPendingSyncIfUnchanged("1", updatedAt = 5L)
        assertTrue(dao.getPendingSync().isEmpty())
    }

    @Test
    fun getByIdsReturnsRequestedRowsIncludingTombstones() = runTest {
        dao.insert(PostingEntity(id = "1", narrative = "live"))
        dao.insert(PostingEntity(id = "2", narrative = "gone", isDeleted = true))
        dao.insert(PostingEntity(id = "3", narrative = "other"))

        val rows = dao.getByIds(listOf("1", "2")).map { it.id }.sorted()
        assertEquals(listOf("1", "2"), rows)
    }
}