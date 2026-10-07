package app.oreshkov.ledger.core.bootstrap.di

import androidx.navigation3.runtime.NavKey
import app.oreshkov.ledger.feature.posting.api.navigation.PostingDetail
import app.oreshkov.ledger.feature.posting.api.navigation.PostingEdit
import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import app.oreshkov.ledger.feature.settings.api.navigation.SettingsHome
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for BootstrapModule provider logic.
 * (Integration testing is handled in androidApp and desktopApp modules)
 */
class BootstrapModuleTest {

    @Test
    fun verifyStartDestination() {
        val bootstrap = BootstrapModule()
        val startDestination = bootstrap.startDestination()
        
        assertEquals(PostingList, startDestination.key)
    }

    /**
     * The per-feature NavKey tests each check their own serializers module alone; this pins
     * the *combined* configuration App() hands to rememberNavBackStack/rememberSerializable.
     * A feature module dropped from it only fails at runtime, when the back stack is saved.
     *
     * Resolves the same polymorphic lookups the saved-state encoder and decoder make, rather
     * than round-tripping through encodeToSavedState: on the Android host that writes into a
     * stubbed Bundle.
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun savedStateConfiguration_resolvesEveryFeatureNavKey() {
        val module = BootstrapModule().savedStateConfiguration().serializersModule
        val keys: List<NavKey> = listOf(PostingList, PostingDetail("1"), PostingEdit(null), SettingsHome)

        for (key in keys) {
            val encoder = assertNotNull(module.getPolymorphic(NavKey::class, key), "no encoder for $key")
            val serialName = encoder.descriptor.serialName
            val decoder = assertNotNull(module.getPolymorphic(NavKey::class, serialName), "no decoder for $serialName")
            assertEquals(serialName, decoder.descriptor.serialName)
        }
    }
}
