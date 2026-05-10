package app.oreshkov.kmpledger.core.data.repository

import app.oreshkov.kmpledger.core.database.dao.PostingDao
import app.oreshkov.kmpledger.core.database.model.PostingEntity
import app.oreshkov.kmpledger.core.model.data.Posting
import app.oreshkov.kmpledger.core.model.data.NewPosting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import kotlin.time.Instant

class OfflineFirstPostingRepositoryTest {

    private val fakeDao = FakePostingDao()
    private val repository = OfflineFirstPostingRepository(fakeDao)

    @Test
    fun getAllPostings_mapsEntitiesToModels() = runTest {
        val entity = PostingEntity(1, 100L, Instant.fromEpochMilliseconds(1000L), "USD", "Fuel")
        fakeDao.insert(entity)

        val postings = repository.getAllPostings().first()
        assertEquals(1, postings.size)
        assertEquals(100L, postings[0].amount)
        assertEquals("USD", postings[0].currency)
    }

    @Test
    fun insertPosting_mapsNewPostingToEntity() = runTest {
        val newPosting = NewPosting(100L, Instant.fromEpochMilliseconds(1000), "USD", "Fuel")
        repository.insertPosting(newPosting)

        val entities = fakeDao.entities.value
        assertEquals(1, entities.size)
        assertEquals(100L, entities[0].amount)
        assertEquals("USD", entities[0].currency)
    }

    @Test
    fun deletePosting_callsDaoDelete() = runTest {
        val entity = PostingEntity(1, 100L, Instant.fromEpochMilliseconds(1000L), "USD", "Fuel")
        fakeDao.insert(entity)
        val posting = Posting(1, 100L, Instant.fromEpochMilliseconds(1000L), "USD", "Fuel")

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