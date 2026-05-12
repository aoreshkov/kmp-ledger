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
        val posting = PostingEntity(id = 1, narrative = "Monthly rent")
        dao.insert(posting)

        val loaded = dao.getPostingById(1).first()
        assertEquals(posting, loaded)
    }

    @Test
    fun getAllPostings() = runTest {
        val posting1 = PostingEntity(id = 1, narrative = "Monthly rent")
        val posting2 = PostingEntity(id = 2, narrative = "Grocery")
        dao.insert(posting1)
        dao.insert(posting2)

        val allPostings = dao.getAllPostings().first()
        assertEquals(2, allPostings.size)
        assertEquals(listOf(posting1, posting2), allPostings)
    }

    @Test
    fun updatePosting() = runTest {
        val posting = PostingEntity(id = 1, narrative = "Fuel")
        dao.insert(posting)

        val updatedPosting = posting.copy(narrative = "Fuel updated")
        dao.update(updatedPosting)

        val loaded = dao.getPostingById(1).first()
        assertEquals("Fuel updated", loaded?.narrative)
    }

    @Test
    fun deletePosting() = runTest {
        val posting = PostingEntity(id = 1, narrative = "Fuel")
        dao.insert(posting)
        dao.delete(posting)

        val loaded = dao.getPostingById(1).first()
        assertNull(loaded)
    }
}