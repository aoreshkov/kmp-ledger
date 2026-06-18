package app.oreshkov.ledger

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.room3.Room
import androidx.room3.RoomDatabase
import app.oreshkov.ledger.core.database.LedgerDatabase
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.module
import kotlin.test.Test
import org.koin.plugin.module.dsl.koinApplication

@OptIn(ExperimentalTestApi::class)
class DesktopUiTest {

    private val inMemoryDatabaseModule = module {
        single<RoomDatabase.Builder<LedgerDatabase>> {
            Room.inMemoryDatabaseBuilder<LedgerDatabase>()
        }
    }

    @Test
    fun app_starts_and_shows_posting_list() = runDesktopComposeUiTest {
        setContent {
            KoinIsolatedContext(
                context = koinApplication<LedgerApp> {
                    allowOverride(override = true)
                    modules(postingNavigationModule, inMemoryDatabaseModule)
                }
            ) {
                App()
            }
        }

        onNodeWithText("My Postings").assertExists()
    }
}