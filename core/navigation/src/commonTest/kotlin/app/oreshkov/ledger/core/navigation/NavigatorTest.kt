package app.oreshkov.ledger.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigatorTest {

    @Serializable
    private data class TestKey(val name: String) : NavKey

    private val start = TestKey("home")
    private val backStack = mutableStateListOf<NavKey>(start)
    private val nav   = Navigator(backStack)

    @Test
    fun initialBackStackContainsStart() {
        assertEquals<List<NavKey>>(listOf(start), nav.entries)
    }

    @Test
    fun canGoBack_falseWithSingleEntry() {
        assertFalse(nav.canGoBack())
    }

    @Test
    fun canGoBack_trueAfterGoTo() {
        nav.goTo(TestKey("detail"))
        assertTrue(nav.canGoBack())
    }

    @Test
    fun goBack_removesLastDestination() {
        nav.goTo(TestKey("detail"))
        nav.goBack()
        assertEquals<List<NavKey>>(listOf(start), nav.entries)
    }

    @Test
    fun goBack_doesNotEmptyStackBelowStart() {
        nav.goBack()
        assertEquals<List<NavKey>>(listOf(start), nav.entries)
    }

    @Test
    fun goTo_appendsDestination() {
        nav.goTo(TestKey("a"))
        nav.goTo(TestKey("b"))
        assertEquals<List<NavKey>>(listOf(start, TestKey("a"), TestKey("b")), nav.entries)
    }

    @Test
    fun switchTopLevel_resetsDeepStackToSection() {
        nav.goTo(TestKey("a"))
        nav.goTo(TestKey("b"))
        nav.switchTopLevel(TestKey("settings"))
        assertEquals<List<NavKey>>(listOf(TestKey("settings")), nav.entries)
    }

    @Test
    fun switchTopLevel_toCurrentRootWhileDrilledIn_returnsToRoot() {
        nav.goTo(TestKey("detail"))
        nav.switchTopLevel(start)
        assertEquals<List<NavKey>>(listOf(start), nav.entries)
    }

    @Test
    fun switchTopLevel_toCurrentRootAtRoot_isNoOp() {
        nav.switchTopLevel(start)
        assertEquals<List<NavKey>>(listOf(start), nav.entries)
    }
}