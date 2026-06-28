package app.oreshkov.ledger.feature.settings.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The NavKeys are the navigation + saved-state contract: a serialization break would
 * silently corrupt back-stack restoration. Pins both per-type round-trips and the
 * polymorphic [serializerSettings] module used by Navigation3.
 */
class SettingsNavKeysTest {

    private val polymorphicJson = Json { serializersModule = serializerSettings }

    @Test
    fun settingsHome_roundTrips() {
        val encoded = Json.encodeToString(SettingsHome.serializer(), SettingsHome)
        assertEquals(SettingsHome, Json.decodeFromString(SettingsHome.serializer(), encoded))
    }

    @Test
    fun navKey_polymorphic_roundTripsThroughSupertype() {
        val serializer = PolymorphicSerializer(NavKey::class)
        val route: SettingsRoute = SettingsHome

        val encoded = polymorphicJson.encodeToString(serializer, route)
        val decoded = polymorphicJson.decodeFromString(serializer, encoded)

        assertEquals(route, decoded)
        assertTrue(decoded is SettingsRoute, "decoded $decoded is not a SettingsRoute")
    }
}
