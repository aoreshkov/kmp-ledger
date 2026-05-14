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
import kotlin.test.assertTrue

class OfflineFirstPostingRepositoryTest {

    private val fakeDao = FakePostingDao()
    private val repository = OfflineFirstPostingRepository(fakeDao)

    @Test
    fun getAllPostings_mapsEntitiesToModels() = runTest {
        val entity = PostingEntity(1, "Groceries")
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
        val entity = PostingEntity(1, "Groceries")
        fakeDao.insert(entity)
        val posting = Posting(1, "Groceries")

        repository.deletePosting(posting)

        val remaining = fakeDao.entities.value
        assertTrue(remaining.isEmpty())
    }
}

class FakePostingDao : PostingDao {
    val entities = MutableStateFlow<List<PostingEntity>>(emptyList())

    override suspend fun insert(posting: PostingEntity) {
        entities.value = entities.value + posting
    }

    override suspend fun delete(posting: PostingEntity) {
        entities.value = entities.value.filterNot { it.id == posting.id }
    }

    override suspend fun update(posting: PostingEntity) {
        entities.value = entities.value.map { if (it.id == posting.id) posting else it }
    }

    override fun getAllPostings(): Flow<List<PostingEntity>> = entities

    override fun getPostingById(id: Long): Flow<PostingEntity?> = entities.map { list -> list.find { it.id == id } }
}