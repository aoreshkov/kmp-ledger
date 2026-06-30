package app.oreshkov.ledger

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import androidx.room3.RoomDatabase
import app.oreshkov.ledger.core.database.LedgerDatabase
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import app.oreshkov.ledger.feature.settings.impl.di.settingsNavigationModule
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.module
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import org.koin.plugin.module.dsl.koinApplication

@OptIn(ExperimentalTestApi::class)
class DesktopUiTest {

    // JUnit instantiates the test class per method, so each test gets its own temp dir
    // (and thus its own DataStore file) — DataStore forbids two active instances on one file.
    private val tempDir: File = Files.createTempDirectory("ledger-desktop-test").toFile()

    @AfterTest
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    private val inMemoryDatabaseModule = module {
        single<RoomDatabase.Builder<LedgerDatabase>> {
            Room.inMemoryDatabaseBuilder<LedgerDatabase>()
        }
    }

    // Overrides the real desktop DataStore (which targets a fixed %APPDATA% path) so tests
    // neither collide on one file nor touch the developer's real settings.
    private val testDataStoreModule = module {
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.create { File(tempDir, "ledger.preferences_pb") }
        }
    }

    @Test
    fun app_starts_and_shows_posting_list() = runDesktopComposeUiTest {
        setContent {
            KoinIsolatedContext(
                context = koinApplication<LedgerApp> {
                    allowOverride(override = true)
                    modules(postingNavigationModule, inMemoryDatabaseModule, testDataStoreModule)
                }
            ) {
                App()
            }
        }

        onNodeWithText("My Postings").assertExists()
    }

    @Test
    fun app_navigatesFromListToAddScreen() = runDesktopComposeUiTest {
        setContent {
            KoinIsolatedContext(
                context = koinApplication<LedgerApp> {
                    allowOverride(override = true)
                    modules(postingNavigationModule, inMemoryDatabaseModule, testDataStoreModule)
                }
            ) {
                App()
            }
        }

        // Real graph: real NavDisplay + real ViewModels over a real in-memory Room DB.
        onNodeWithText("My Postings").assertExists()
        onNodeWithContentDescription("Add Posting").performClick()
        onNodeWithText("Add Posting").assertExists()
    }

    @Test
    fun inProgressPostingSurvivesTabSwitch() = runDesktopComposeUiTest {
        setContent {
            KoinIsolatedContext(
                context = koinApplication<LedgerApp> {
                    allowOverride(override = true)
                    modules(
                        postingNavigationModule,
                        settingsNavigationModule,
                        inMemoryDatabaseModule,
                        testDataStoreModule,
                    )
                }
            ) {
                App()
            }
        }

        // Start adding a posting, hop to Settings and back: the in-progress add screen must
        // remain (per-section back stacks), not reset to the list.
        onNodeWithContentDescription("Add Posting").performClick()
        onNodeWithText("Add Posting").assertExists()

        onNodeWithText("Settings").performClick()
        onNodeWithText("Postings").performClick()

        onNodeWithText("Add Posting").assertExists()
    }
}