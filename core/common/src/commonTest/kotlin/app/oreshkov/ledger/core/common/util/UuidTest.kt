package app.oreshkov.ledger.core.common.util

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UuidTest {

    @Test
    fun randomUuidString_returnsNonEmptyString() {
        val uuid = randomUuidString()
        assertTrue(uuid.isNotEmpty())
    }

    @Test
    fun randomUuidString_returnsUniqueStrings() {
        val uuid1 = randomUuidString()
        val uuid2 = randomUuidString()
        assertNotEquals(uuid1, uuid2)
    }

    @Test
    fun randomUuidString_matchesExpectedFormat() {
        val uuid = randomUuidString()
        // Standard UUID format: 8-4-4-4-12 hex characters
        val regex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        assertTrue(regex.matches(uuid), "UUID '$uuid' does not match the expected format")
    }
}