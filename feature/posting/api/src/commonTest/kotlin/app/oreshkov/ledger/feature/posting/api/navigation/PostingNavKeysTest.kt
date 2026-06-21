package app.oreshkov.ledger.feature.posting.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * The NavKeys are the navigation + saved-state contract: a serialization break would
 * silently corrupt back-stack restoration. These tests pin both the per-type
 * (de)serialization and the polymorphic [serializerPostings] module used by Navigation3.
 */
class PostingNavKeysTest {

    private val polymorphicJson = Json { serializersModule = serializerPostings }

    @Test
    fun postingList_roundTrips() {
        val encoded = Json.encodeToString(PostingList.serializer(), PostingList)
        assertEquals(PostingList, Json.decodeFromString(PostingList.serializer(), encoded))
    }

    @Test
    fun postingDetail_roundTrips() {
        val original = PostingDetail("1")
        val encoded = Json.encodeToString(PostingDetail.serializer(), original)
        assertEquals(original, Json.decodeFromString(PostingDetail.serializer(), encoded))
    }

    @Test
    fun postingEdit_withNullId_roundTrips() {
        val original = PostingEdit(null)
        val encoded = Json.encodeToString(PostingEdit.serializer(), original)
        val decoded = Json.decodeFromString(PostingEdit.serializer(), encoded)
        assertEquals(original, decoded)
        assertEquals(null, decoded.id)
    }

    @Test
    fun postingEdit_withNonNullId_roundTrips() {
        val original = PostingEdit("42")
        val encoded = Json.encodeToString(PostingEdit.serializer(), original)
        assertEquals(original, Json.decodeFromString(PostingEdit.serializer(), encoded))
    }

    @Test
    fun navKey_polymorphic_roundTripsThroughSupertype() {
        val serializer = PolymorphicSerializer(NavKey::class)
        val routes: List<PostingRoute> = listOf(
            PostingList,
            PostingDetail("1"),
            PostingEdit(null),
            PostingEdit("42"),
        )
        for (route in routes) {
            val encoded = polymorphicJson.encodeToString(serializer, route)
            val decoded = polymorphicJson.decodeFromString(serializer, encoded)
            assertEquals(route, decoded)
            assertTrue(decoded is PostingRoute, "decoded $decoded is not a PostingRoute")
        }
    }

    @Test
    fun postingDetail_distinctIds_areNotEqual() {
        assertNotEquals(PostingDetail("1"), PostingDetail("2"))
    }

    @Test
    fun decodingWithMissingRequiredId_fails() {
        // Guards the saved-state contract: a corrupted/incomplete back-stack entry
        // missing a required field must be rejected, not silently defaulted.
        // Exercises the serialization-generated "missing field" branch in each ctor.
        assertFailsWith<SerializationException> {
            Json.decodeFromString(PostingDetail.serializer(), "{}")
        }
        assertFailsWith<SerializationException> {
            Json.decodeFromString(PostingEdit.serializer(), "{}")
        }
    }

    @Test
    fun navKeys_equalsEdgeCases() {
        val detail = PostingDetail("1")
        val edit = PostingEdit("1")
        // Call equals directly so each generated branch is exercised; `==` against
        // null/other types short-circuits before equals is invoked.
        // identity short-circuit: this === other
        assertTrue(detail.equals(detail))
        assertTrue(edit.equals(edit))
        // type/null mismatch: other !is T
        assertFalse(detail.equals(null))
        assertFalse(detail.equals("not-a-route"))
        assertFalse(edit.equals(null))
        assertFalse(edit.equals(42))
    }
}
