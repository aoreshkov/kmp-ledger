package app.oreshkov.ledger.core.database

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class LedgerDatabaseTest {

    private lateinit var db: LedgerDatabase

    @BeforeTest
    fun setUp() {
        db = createTestDatabase()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun testDatabaseInitialization() {
        assertNotNull(db)
    }

    @Test
    fun testPostingDaoIsAccessible() {
        val postingDao = db.postingDao()
        assertNotNull(postingDao)
    }
}