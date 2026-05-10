package app.oreshkov.kmpledger.core.database

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class KMPLedgerDatabaseTest {

    private lateinit var db: KMPLedgerDatabase

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