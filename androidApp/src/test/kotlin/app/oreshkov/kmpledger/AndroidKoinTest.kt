package app.oreshkov.kmpledger

import androidx.test.core.app.ApplicationProvider
import app.oreshkov.kmpledger.core.database.KMPLedgerDatabase
import app.oreshkov.kmpledger.core.navigation.Navigator
import app.oreshkov.kmpledger.feature.posting.impl.PostingListViewModel
import app.oreshkov.kmpledger.feature.posting.impl.di.postingNavigationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.plugin.module.dsl.startKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.After
import org.junit.Test
import org.junit.Assert.assertNotNull
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidKoinTest : KoinTest {

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }

    @Test
    fun verifyKoinConfiguration() {
        GlobalContext.stopKoin() // Ensure fresh start
        startKoin<KMPLedgerApp> {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(postingNavigationModule)
        }

        // Verify resolution of critical platform-aware components
        assertNotNull(get<Navigator>())
        assertNotNull(get<KMPLedgerDatabase>())
        assertNotNull(get<PostingListViewModel>())
    }
}
