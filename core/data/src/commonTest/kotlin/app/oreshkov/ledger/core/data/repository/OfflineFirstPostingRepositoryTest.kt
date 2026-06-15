package app.oreshkov.ledger.core.data.repository

import app.oreshkov.ledger.core.database.dao.PostingDao
import app.oreshkov.ledger.core.database.model.PostingEntity
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OfflineFirstPostingRepositoryTest {

    private val fakeDao = FakePostingDao()
    private val repository = OfflineFirstPostingRepository(fakeDao)

    @Test
    fun getAllPostings_mapsEntitiesToModels() = runTest {
        val entity = PostingEntity("1", "Groceries")
        fakeDao.insert(entity)

        val postings = repository.getAllPostings().first()
        assertEquals(1, postings.size)
        assertEquals("Groceries", postings[0].narrative)
    }

    @Test
    fun insertPosting_mapsNewPostingToEntity() = runTest {
        val newPosting = NewPosting("Groceries")
        repository.insertPosting(newPosting)

        val entities = fakeDao.entities.value
        assertEquals(1, entities.size)
        assertEquals("Groceries", entities[0].narrative)
    }

    @Test
    fun deletePosting_callsDaoDelete() = runTest {
        val entity = PostingEntity("1", "Groceries")
        fakeDao.insert(entity)
        val posting = Posting("1", "Groceries")

        repository.deletePosting(posting.id)

        val remaining = fakeDao.entities.value
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun updatePosting_mapsToEntityAndCallsDao() = runTest {
        val initialEntity = PostingEntity("1", "Old")
        fakeDao.insert(initialEntity)
        val updatedPosting = Posting("1", "New")

        repository.updatePosting(updatedPosting)

        val entities = fakeDao.entities.value
        assertEquals(1, entities.size)
        assertEquals("New", entities[0].narrative)
    }

    @Test
    fun getPostingById_returnsMappedModel() = runTest {
        val entity = PostingEntity("1", "Groceries")
        fakeDao.insert(entity)

        val posting = repository.getPostingById("1").first()
        assertEquals("1", posting?.id)
        assertEquals("Groceries", posting?.narrative)
    }

    @Test
    fun getPostingById_returnsNullWhenNotFound() = runTest {
        val nonExistent = repository.getPostingById("99").first()
        assertNull(nonExistent)
    }
}

class FakePostingDao : PostingDao {
    val entities = MutableStateFlow<List<PostingEntity>>(emptyList())

    override suspend fun insert(posting: PostingEntity) {
        entities.value = entities.value + posting
    }

    override suspend fun deleteById(id: String) {
        entities.value = entities.value.filterNot { it.id == id }
    }

    override suspend fun update(posting: PostingEntity) {
        entities.value = entities.value.map { if (it.id == posting.id) posting else it }
    }

    override fun getAllPostings(): Flow<List<PostingEntity>> = entities

    override fun getPostingById(id: String): Flow<PostingEntity?> = entities.map { list -> list.find { it.id == id } }
}