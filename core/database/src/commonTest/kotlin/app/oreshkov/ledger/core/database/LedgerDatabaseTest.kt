package app.oreshkov.ledger.core.database

import kotlin.test.Test
import kotlin.test.assertNotNull

class LedgerDatabaseTest {

    @Test
    fun database_providesDao() {
        val db = createTestDatabase()
        assertNotNull(db.postingDao())
        db.close()
    }
}