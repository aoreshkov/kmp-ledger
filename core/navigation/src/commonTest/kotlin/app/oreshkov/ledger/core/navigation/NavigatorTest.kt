package app.oreshkov.ledger.core.navigation

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
    private val nav   = Navigator(start)

    @Test
    fun initialBackStackContainsStart() {
        assertEquals(listOf(start), nav.backStack)
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
        assertEquals(listOf(start), nav.backStack)
    }

    @Test
    fun goBack_doesNotEmptyStackBelowStart() {
        nav.goBack()
        assertEquals(listOf(start), nav.backStack)
    }

    @Test
    fun goTo_appendsDestination() {
        nav.goTo(TestKey("a"))
        nav.goTo(TestKey("b"))
        assertEquals(listOf(start, TestKey("a"), TestKey("b")), nav.backStack)
    }

    @Test
    fun bind_replacesActiveBackStack() {
        val newStack = androidx.compose.runtime.mutableStateListOf<NavKey>(TestKey("other"))
        nav.bind(newStack)

        nav.goTo(TestKey("detail"))

        // Verify via the Navigator's public API
        assertEquals(listOf(TestKey("other"), TestKey("detail")), nav.backStack)
    }
}