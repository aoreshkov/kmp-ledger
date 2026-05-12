package app.oreshkov.ledger.core.bootstrap.di

import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test
    fun verifySavedStateConfiguration() {
        val bootstrap = BootstrapModule()
        val config = bootstrap.savedStateConfiguration()
        
        // Ensure configuration is successfully created
        // (Deep inspection of serializersModule is limited by the library API)
        assert(config.serializersModule != null)
    }
}
