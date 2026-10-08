package app.oreshkov.ledger.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.compose.resources.Res
import app.oreshkov.ledger.core.compose.resources.back_content_description
import app.oreshkov.ledger.core.navigation.TopLevelDestination
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Drives the real `App()` through a save-and-restore cycle: the selected top-level section must
 * survive it, and a restored section that no longer exists must fall back to the start route
 * rather than crash `App()`'s `getValue` lookups (`rememberSerializable` does not validate a
 * restored value against its inputs, so `App()` guards it after restoration).
 *
 * The start route is `AppTestModule`'s `TestKey("test")`; [settingsSection] adds a second section.
 * Its label borrows `core:compose`'s public string because `core:ui` has no resources of its own
 * and test-source-set Compose resources are not packaged for Android host tests.
 *
 * **Android-only on purpose.** [StateRestorationTester] cannot run on the skiko targets:
 * `platformEncodeDecode` is `TODO("Not yet implemented")` there, so the same test in
 * `commonTest` fails on jvm with `NotImplementedError`. Tracked upstream as CMP-6836
 * ("Compose UI Test StateRestorationTester does not work on Non Android Targets", open).
 * Note the in-source TODO cites CMP-7992, which is a different, already-fixed issue.
 * Move this to `commonTest` once CMP-6836 ships.
 */
@OptIn(ExperimentalTestApi::class, KoinExperimentalAPI::class, ExperimentalCoroutinesApi::class)
class AppStateRestorationTest : PlatformComposeUiTest() {

    private val sectionEntries = module {
        navigation<TestKey> { key -> Text("screen=${key.id}") }
    }

    private val settingsSection = module {
        single(named("settings_top_level")) {
            TopLevelDestination(
                key = TestKey("settings"),
                label = Res.string.back_content_description,
                icon = Icons.Filled.Settings,
                order = 1,
            )
        }
    }

    // Isolated contexts, not the KoinApplication composable: its loader keeps the global Koin
    // alive when forgotten and re-attaches to it on recreation, so the restored composition
    // would silently get the pre-save graph back.
    private val withSettings = koinApplication<TestApp> { modules(sectionEntries, settingsSection) }
    private val withoutSettings = koinApplication<TestApp> { modules(sectionEntries) }

    // App() collects the theme flow via collectAsStateWithLifecycle (see AppTest).
    @BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @AfterTest
    fun tearDown() {
        withSettings.close()
        withoutSettings.close()
        Dispatchers.resetMain()
    }

    /** Opens the app with the settings section, selects it, then restores into [after]'s graph. */
    private fun ComposeUiTest.selectSettingsThenRestore(after: KoinApplication) {
        val tester = StateRestorationTester(this)
        var koin = withSettings
        tester.setContent {
            KoinIsolatedContext(context = koin) { App() }
        }
        onNodeWithText("screen=test").assertIsDisplayed()

        onNodeWithText("Back").performClick()
        onNodeWithText("screen=settings").assertIsDisplayed()

        koin = after
        tester.emulateSaveAndRestore()
    }

    @Test
    fun selectedSection_survivesSaveAndRestore() = runComposeUiTest {
        // Control for the fallback test below: without it, a restore that silently lost every
        // value would also "fall back" to the start route.
        selectSettingsThenRestore(after = withSettings)

        onNodeWithText("screen=settings").assertIsDisplayed()
    }

    @Test
    fun restoredSectionThatNoLongerExists_fallsBackToStart() = runComposeUiTest {
        // Stands in for a section removed between the save and the relaunch.
        selectSettingsThenRestore(after = withoutSettings)

        onNodeWithText("screen=test").assertIsDisplayed()
        onNodeWithText("screen=settings").assertDoesNotExist()
    }
}
