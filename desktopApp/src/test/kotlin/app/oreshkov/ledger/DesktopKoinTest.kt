package app.oreshkov.ledger

import LedgerApp
import app.oreshkov.ledger.core.navigation.Navigator
import app.oreshkov.ledger.core.data.repository.PostingRepository
import app.oreshkov.ledger.core.database.LedgerDatabase
import app.oreshkov.ledger.feature.posting.impl.PostingListViewModel
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.plugin.module.dsl.startKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class DesktopKoinTest : KoinTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun verifyKoinConfiguration() {
        startKoin<LedgerApp> {
            modules(postingNavigationModule)
        }

        assertNotNull(get<Navigator>())
        assertNotNull(get<PostingRepository>())
        assertNotNull(get<LedgerDatabase>())
        assertNotNull(get<PostingListViewModel>())
    }
}