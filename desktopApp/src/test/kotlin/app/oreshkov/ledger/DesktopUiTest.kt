package app.oreshkov.ledger

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
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
        startKoin<LedgerApp> {
            modules(postingNavigationModule)
        }

        setContent {
            App()
        }

        // "My Postings" is the title of the PostingListScreen
        onNodeWithText("My Postings").assertExists()
    }
}