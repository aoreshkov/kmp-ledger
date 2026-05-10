package app.oreshkov.kmpledger

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import app.oreshkov.kmpledger.core.ui.App
import app.oreshkov.kmpledger.feature.posting.impl.di.postingNavigationModule
import org.koin.core.context.stopKoin
import org.koin.plugin.module.dsl.startKoin
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesktopUiTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun app_starts_and_shows_posting_list() = runDesktopComposeUiTest {
        startKoin<KMPLedgerApp> {
            modules(postingNavigationModule)
        }

        setContent {
            App()
        }

        // "My Postings" is the title of the PostingListScreen
        onNodeWithText("My Postings").assertExists()
    }
}