package app.oreshkov.ledger

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import kotlin.test.assertNull
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

    @Test
    fun inProgressPostingSurvivesDoubleTapOnItsSection() = runDesktopComposeUiTest {
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

        // Type a draft, hop to Settings, then double-tap Postings: the second tap re-selects the
        // section it just restored, which must not discard the add screen or its draft.
        onNodeWithContentDescription("Add Posting").performClick()
        onNodeWithText("Narrative").performTextInput("Unsaved draft")

        onNodeWithText("Settings").performClick()
        onNodeWithText("Postings").performClick()
        onNodeWithText("Postings").performClick()

        onNodeWithText("Add Posting").assertExists()
        onNodeWithText("Unsaved draft").assertExists()
    }

    // The deep-link tests below go through the real graph: BootstrapModule's resolver with the
    // shipped `ledger://` table, the real Navigator and the real screens.

    @Test
    fun deepLinkToSettings_opensThatSection_andBackExitsThroughHome() = runDesktopComposeUiTest {
        var pending by mutableStateOf<String?>("ledger://settings")
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
                App(deepLink = pending, onDeepLinkConsumed = { pending = null })
            }
        }

        // "System default" exists only on the settings screen ("Settings" is also the nav label).
        onNodeWithText("System default").assertExists()
        assertNull(pending)

        onNodeWithContentDescription("Back").performClick()
        onNodeWithText("My Postings").assertExists()
    }

    @Test
    fun deepLinkToNewPosting_opensTheAddScreenAboveTheList() = runDesktopComposeUiTest {
        var pending by mutableStateOf<String?>("ledger://postings/new")
        setContent {
            KoinIsolatedContext(
                context = koinApplication<LedgerApp> {
                    allowOverride(override = true)
                    modules(postingNavigationModule, inMemoryDatabaseModule, testDataStoreModule)
                }
            ) {
                App(deepLink = pending, onDeepLinkConsumed = { pending = null })
            }
        }

        onNodeWithText("Narrative").assertExists()

        onNodeWithContentDescription("Back").performClick()
        onNodeWithText("Narrative").assertDoesNotExist()
        onNodeWithText("My Postings").assertExists()
    }

    @Test
    fun deepLinkToAnUnknownPosting_showsNotFound_andGoBackReachesTheList() = runDesktopComposeUiTest {
        // A link is outside input: an id that does not exist must land on the screen's own
        // not-found state, with a way out, rather than an error.
        var pending by mutableStateOf<String?>("ledger://postings/no-such-posting")
        setContent {
            KoinIsolatedContext(
                context = koinApplication<LedgerApp> {
                    allowOverride(override = true)
                    modules(postingNavigationModule, inMemoryDatabaseModule, testDataStoreModule)
                }
            ) {
                App(deepLink = pending, onDeepLinkConsumed = { pending = null })
            }
        }

        onNodeWithText("Posting has been already deleted.").assertExists()

        onNodeWithText("Go back").performClick()
        onNodeWithText("Posting has been already deleted.").assertDoesNotExist()
        onNodeWithText("My Postings").assertExists()
    }

    @Test
    fun deepLinkWhileEditing_replacesThatSectionsStack() = runDesktopComposeUiTest {
        var pending by mutableStateOf<String?>(null)
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
                App(deepLink = pending, onDeepLinkConsumed = { pending = null })
            }
        }

        onNodeWithContentDescription("Add Posting").performClick()
        onNodeWithText("Narrative").performTextInput("Unsaved draft")

        // A link into another section leaves the draft where it was...
        pending = "ledger://settings"
        onNodeWithText("System default").assertExists()
        onNodeWithText("Postings").performClick()
        onNodeWithText("Unsaved draft").assertExists()

        // ...while a link into the section being edited replaces what that section showed.
        pending = "ledger://postings"
        onNodeWithText("Unsaved draft").assertDoesNotExist()
        onNodeWithText("My Postings").assertExists()
    }
}