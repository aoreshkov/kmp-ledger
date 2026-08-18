package app.oreshkov.ledger.core.data.repository

import app.oreshkov.ledger.core.common.dispatcher.AppDispatchers
import app.oreshkov.ledger.core.database.dao.PostingDao
import app.oreshkov.ledger.core.database.model.PostingEntity
import app.oreshkov.ledger.core.test.newPosting
import app.oreshkov.ledger.core.test.posting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstPostingRepositoryTest {

    private val fakeDao = FakePostingDao()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(FIXED_TIME)
    }
    private val repository =
        OfflineFirstPostingRepository(fakeDao, TestAppDispatchers(testDispatcher), fixedClock)

    @Test
    fun getAllPostings_mapsEntitiesToModels() = runTest {
        val entity = PostingEntity("1", "Groceries")
        fakeDao.insert(entity)

        val postings = repository.getAllPostings().first()
        assertEquals(1, postings.size)
        assertEquals("Groceries", postings[0].narrative)
    }

    @Test
    fun insertPosting_stampsSyncMetadata() = runTest {
        repository.insertPosting(newPosting())

        val entities = fakeDao.entities.value
        assertEquals(1, entities.size)
        assertEquals("Groceries", entities[0].narrative)
        assertEquals(FIXED_TIME, entities[0].updatedAt)
        assertTrue(entities[0].pendingSync)
    }

    @Test
    fun deletePosting_softDeletesAndTombstones() = runTest {
        val entity = PostingEntity("1", "Groceries")
        fakeDao.insert(entity)

        repository.deletePosting("1")

        // Hidden from live reads, but retained as a pending tombstone.
        assertTrue(repository.getAllPostings().first().isEmpty())
        val tombstone = fakeDao.entities.value.single()
        assertTrue(tombstone.isDeleted)
        assertTrue(tombstone.pendingSync)
        assertEquals(FIXED_TIME, tombstone.updatedAt)
    }

    @Test
    fun updatePosting_mapsToEntityAndStampsMetadata() = runTest {
        fakeDao.insert(PostingEntity("1", "Old"))
        val updatedPosting = posting(narrative = "New")

        repository.updatePosting(updatedPosting)

        val entity = fakeDao.entities.value.single()
        assertEquals("New", entity.narrative)
        assertEquals(FIXED_TIME, entity.updatedAt)
        assertTrue(entity.pendingSync)
    }

    @Test
    fun getPostingById_returnsMappedModel() = runTest {
        fakeDao.insert(PostingEntity("1", "Groceries"))

        val posting = repository.getPostingById("1").first()
        assertEquals("1", posting?.id)
        assertEquals("Groceries", posting?.narrative)
    }

    @Test
    fun getPostingById_returnsNullWhenNotFound() = runTest {
        assertNull(repository.getPostingById("99").first())
    }

    private companion object {
        const val FIXED_TIME = 1_000L
    }
}

private class TestAppDispatchers(dispatcher: CoroutineDispatcher) : AppDispatchers {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}

class FakePostingDao : PostingDao {
    val entities = MutableStateFlow<List<PostingEntity>>(emptyList())

    override suspend fun insert(posting: PostingEntity) {
        entities.value = entities.value + posting
    }

    override suspend fun update(posting: PostingEntity) {
        entities.value = entities.value.map { if (it.id == posting.id) posting else it }
    }

    override suspend fun softDeleteById(id: String, updatedAt: Long) {
        entities.value = entities.value.map {
            if (it.id == id) it.copy(isDeleted = true, pendingSync = true, updatedAt = updatedAt) else it
        }
    }

    override suspend fun hardDeleteById(id: String) {
        entities.value = entities.value.filterNot { it.id == id }
    }

    override fun getAllPostings(): Flow<List<PostingEntity>> =
        entities.map { list -> list.filterNot { it.isDeleted } }

    override fun getPostingById(id: String): Flow<PostingEntity?> =
        entities.map { list -> list.find { it.id == id && !it.isDeleted } }

    override suspend fun getPendingSync(): List<PostingEntity> =
        entities.value.filter { it.pendingSync }

    override suspend fun getByIds(ids: List<String>): List<PostingEntity> =
        entities.value.filter { it.id in ids }

    override suspend fun upsert(postings: List<PostingEntity>) {
        val byId = postings.associateBy { it.id }
        val updated = entities.value.map { byId[it.id] ?: it }
        val existingIds = entities.value.map { it.id }.toSet()
        val added = postings.filterNot { it.id in existingIds }
        entities.value = updated + added
    }

    override suspend fun clearPendingSyncIfUnchanged(id: String, updatedAt: Long) {
        entities.value = entities.value.map {
            if (it.id == id && it.updatedAt == updatedAt) it.copy(pendingSync = false) else it
        }
    }
}
