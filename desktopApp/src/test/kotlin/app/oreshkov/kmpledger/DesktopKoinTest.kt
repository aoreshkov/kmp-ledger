package app.oreshkov.kmpledger

import app.oreshkov.kmpledger.core.navigation.Navigator
import app.oreshkov.kmpledger.core.data.repository.PostingRepository
import app.oreshkov.kmpledger.core.database.KMPLedgerDatabase
import app.oreshkov.kmpledger.feature.posting.impl.PostingListViewModel
import app.oreshkov.kmpledger.feature.posting.impl.di.postingNavigationModule
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
        startKoin<KMPLedgerApp> {
            modules(postingNavigationModule)
        }

        assertNotNull(get<Navigator>())
        assertNotNull(get<PostingRepository>())
        assertNotNull(get<KMPLedgerDatabase>())
        assertNotNull(get<PostingListViewModel>())
    }
}