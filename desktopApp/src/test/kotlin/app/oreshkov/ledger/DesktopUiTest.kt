package app.oreshkov.ledger

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import org.koin.compose.KoinIsolatedContext
import kotlin.test.Test
import org.koin.plugin.module.dsl.koinApplication

@OptIn(ExperimentalTestApi::class)
class DesktopUiTest {

    @Test
    fun app_starts_and_shows_posting_list() = runDesktopComposeUiTest {
        setContent {
            KoinIsolatedContext(
                context = koinApplication<LedgerApp> {
                    modules(postingNavigationModule)
                }
            ) {
                App()
            }
        }

        onNodeWithText("My Postings").assertExists()
    }
}