package app.oreshkov.ledger.core.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigatorTest {

    @Serializable
    private data class TestKey(val name: String) : NavKey

    private val home = TestKey("home")
    private val settings = TestKey("settings")

    private val homeStack = NavBackStack<NavKey>(home)
    private val settingsStack = NavBackStack<NavKey>(settings)

    private val nav = Navigator(
        startRoute = home,
        backStacks = mapOf(home to homeStack, settings to settingsStack),
        currentTopLevelState = mutableStateOf(home),
    )

    @Test
    fun initialBackStackContainsStart() {
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
        assertEquals(home, nav.currentTopLevel)
    }

    @Test
    fun canGoBack_falseAtStartRoot() {
        assertFalse(nav.canGoBack())
    }

    @Test
    fun canGoBack_trueAfterGoTo() {
        nav.goTo(TestKey("detail"))
        assertTrue(nav.canGoBack())
    }

    @Test
    fun canGoBack_trueWhenAwayFromStartSection() {
        nav.switchTopLevel(settings)
        assertTrue(nav.canGoBack())
    }

    @Test
    fun goTo_appendsToCurrentSection() {
        nav.goTo(TestKey("a"))
        nav.goTo(TestKey("b"))
        assertEquals<List<NavKey>>(listOf(home, TestKey("a"), TestKey("b")), nav.entries)
    }

    @Test
    fun goBack_popsWithinCurrentSection() {
        nav.goTo(TestKey("detail"))
        nav.goBack()
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
    }

    @Test
    fun goBack_atStartRoot_isNoOp() {
        nav.goBack()
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
    }

    @Test
    fun goBack_atSectionRoot_returnsToStartSection() {
        nav.switchTopLevel(settings)
        nav.goBack()
        assertEquals(home, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
    }

    @Test
    fun switchTopLevel_preservesOtherSectionStack() {
        // Drill into the start section, then leave and come back.
        nav.goTo(TestKey("a"))
        nav.goTo(TestKey("b"))
        nav.switchTopLevel(settings)
        nav.switchTopLevel(home)
        // The in-progress home stack must be intact (the regression this change fixes).
        assertEquals<List<NavKey>>(listOf(home, TestKey("a"), TestKey("b")), nav.entries)
    }

    @Test
    fun switchTopLevel_toAnotherSection_concatenatesStartThenCurrent() {
        nav.switchTopLevel(settings)
        nav.goTo(TestKey("settings-detail"))
        assertEquals<List<NavKey>>(
            listOf(home, settings, TestKey("settings-detail")),
            nav.entries,
        )
    }

    @Test
    fun switchTopLevel_toCurrentSectionWhileDrilledIn_resetsToRoot() {
        nav.switchTopLevel(settings)
        nav.goTo(TestKey("settings-detail"))
        nav.switchTopLevel(settings)
        assertEquals(settings, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, settings), nav.entries)
    }

    @Test
    fun switchTopLevel_toCurrentSectionAtRoot_isNoOp() {
        nav.switchTopLevel(home)
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
    }
}
