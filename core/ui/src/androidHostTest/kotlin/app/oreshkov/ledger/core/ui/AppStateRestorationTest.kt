package app.oreshkov.ledger.core.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the save/restore contract behind `App()`'s selected top-level section: the
 * `rememberSerializable` + `PolymorphicSerializer(NavKey::class)` + injected
 * [SavedStateConfiguration] combination must round-trip a [NavKey] through a real
 * save-and-restore cycle, and a restored key that no longer names a section must not
 * survive (`App()` falls back to the start route, since `Navigator` looks its stack up
 * with `getValue`).
 *
 * **Android-only on purpose.** [StateRestorationTester] cannot run on the skiko targets:
 * `platformEncodeDecode` is `TODO("Not yet implemented")` there, so the same test in
 * `commonTest` fails on jvm with `NotImplementedError`. Tracked upstream as CMP-6836
 * ("Compose UI Test StateRestorationTester does not work on Non Android Targets", open).
 * Note the in-source TODO cites CMP-7992, which is a different, already-fixed issue.
 * Move this to `commonTest` once CMP-6836 ships.
 */
@OptIn(ExperimentalTestApi::class)
class AppStateRestorationTest : PlatformComposeUiTest() {

    private val configuration = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) { subclass(TestKey::class) }
        }
    }

    @Test
    fun selectedSection_survivesSaveAndRestore() = runComposeUiTest {
        val tester = StateRestorationTester(this)
        var selected: MutableState<NavKey>? = null

        tester.setContent {
            val state = rememberSerializable(
                stateSerializer = PolymorphicSerializer(NavKey::class),
                configuration = configuration,
            ) { mutableStateOf<NavKey>(TestKey("home")) }
            selected = state
            Text("selected=${(state.value as TestKey).id}")
        }

        runOnIdle { selected!!.value = TestKey("settings") }
        onNodeWithText("selected=settings").assertIsDisplayed()

        tester.emulateSaveAndRestore()

        onNodeWithText("selected=settings").assertIsDisplayed()
    }

    @Test
    fun restoredSectionThatNoLongerExists_fallsBackToStart() = runComposeUiTest {
        val tester = StateRestorationTester(this)
        val start: NavKey = TestKey("home")
        var selected: MutableState<NavKey>? = null
        // Sections present after the restore; "settings" is deliberately absent, standing in
        // for a section removed between the save and the relaunch.
        var sections: Set<NavKey> = setOf(start, TestKey("settings"))

        tester.setContent {
            val state = rememberSerializable(
                stateSerializer = PolymorphicSerializer(NavKey::class),
                configuration = configuration,
            ) { mutableStateOf(start) }
            // Same guard App() applies; rememberSerializable does not validate a restored
            // value against its inputs, so the check has to happen after restoration.
            if (state.value !in sections) state.value = start
            selected = state
            Text("selected=${(state.value as TestKey).id}")
        }

        runOnIdle { selected!!.value = TestKey("settings") }
        onNodeWithText("selected=settings").assertIsDisplayed()

        runOnIdle { sections = setOf(start) }
        tester.emulateSaveAndRestore()

        onNodeWithText("selected=home").assertIsDisplayed()
        runOnIdle { assertEquals(start, selected!!.value) }
    }
}
