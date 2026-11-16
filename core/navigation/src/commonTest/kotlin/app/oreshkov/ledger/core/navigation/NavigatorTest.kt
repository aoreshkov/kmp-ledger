package app.oreshkov.ledger.core.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
    fun goTo_currentTop_isNoOp() {
        // Re-tapping the selected row in the two-pane layout must not stack a duplicate entry.
        val detail = TestKey("detail")
        nav.goTo(detail)
        nav.goTo(detail)
        assertEquals<List<NavKey>>(listOf(home, detail), nav.entries)
    }

    @Test
    fun goTo_sameKeyNotOnTop_appends() {
        val detail = TestKey("detail")
        nav.goTo(detail)
        nav.goTo(TestKey("edit"))
        nav.goTo(detail)
        assertEquals<List<NavKey>>(listOf(home, detail, TestKey("edit"), detail), nav.entries)
    }

    @Test
    fun goTo_sectionRoot_isNoOp() {
        nav.goTo(home)
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
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
    fun goBackFrom_topEntry_pops() {
        val detail = TestKey("detail")
        nav.goTo(detail)
        nav.goBack(from = detail)
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
    }

    @Test
    fun goBackFrom_repeated_popsOnlyOnce() {
        // A double tap on the edit screen's back arrow must not also pop the details screen.
        val detail = TestKey("detail")
        val edit = TestKey("edit")
        nav.goTo(detail)
        nav.goTo(edit)
        nav.goBack(from = edit)
        nav.goBack(from = edit)
        assertEquals<List<NavKey>>(listOf(home, detail), nav.entries)
    }

    @Test
    fun goBackFrom_entryNotOnTop_isNoOp() {
        val detail = TestKey("detail")
        val edit = TestKey("edit")
        nav.goTo(detail)
        nav.goTo(edit)
        nav.goBack(from = detail)
        assertEquals<List<NavKey>>(listOf(home, detail, edit), nav.entries)
    }

    @Test
    fun goBackFrom_entryInAnotherSection_isNoOp() {
        val detail = TestKey("detail")
        nav.goTo(detail)
        nav.switchTopLevel(settings)
        nav.goBack(from = detail)
        assertEquals(settings, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, detail, settings), nav.entries)
    }

    @Test
    fun goBackFrom_sectionRoot_returnsToStartSectionOnce() {
        // The repeat must not pop what the start section was drilled into.
        val detail = TestKey("detail")
        nav.goTo(detail)
        nav.switchTopLevel(settings)
        nav.goBack(from = settings)
        nav.goBack(from = settings)
        assertEquals(home, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, detail), nav.entries)
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
    fun goBack_inDrilledNonStartSection_popsWithinThatSection() {
        // Popping must win over the fall-back-to-start branch while the section has depth.
        nav.switchTopLevel(settings)
        nav.goTo(TestKey("settings-detail"))
        nav.goBack()
        assertEquals(settings, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, settings), nav.entries)
    }

    @Test
    fun switchTopLevel_preservesDrilledNonStartSectionStack() {
        nav.switchTopLevel(settings)
        nav.goTo(TestKey("settings-detail"))
        nav.switchTopLevel(home)
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
        nav.switchTopLevel(settings)
        assertEquals<List<NavKey>>(
            listOf(home, settings, TestKey("settings-detail")),
            nav.entries,
        )
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
    fun switchTopLevel_toCurrentSectionWhileDrilledIn_keepsStackAndSignalsReselect() = runTest {
        val reselections = collectReselections()
        nav.switchTopLevel(settings)
        nav.goTo(TestKey("settings-detail"))
        nav.switchTopLevel(settings)
        assertEquals(settings, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, settings, TestKey("settings-detail")), nav.entries)
        assertEquals<List<NavKey>>(listOf(settings), reselections)
    }

    @Test
    fun switchTopLevel_toCurrentSectionAtRoot_keepsStackAndSignalsReselect() = runTest {
        val reselections = collectReselections()
        nav.switchTopLevel(home)
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
        assertEquals<List<NavKey>>(listOf(home), reselections)
    }

    @Test
    fun switchTopLevel_doubleTapOnDrilledSection_preservesItsStack() = runTest {
        // A double tap on another section's item: the first tap switches, the second lands on
        // the now-current section and must not discard the stack the first one restored.
        val reselections = collectReselections()
        nav.goTo(TestKey("edit"))
        nav.switchTopLevel(settings)
        nav.switchTopLevel(home)
        nav.switchTopLevel(home)
        assertEquals(home, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, TestKey("edit")), nav.entries)
        assertEquals<List<NavKey>>(listOf(home), reselections)
    }

    @Test
    fun switchTopLevel_toAnotherSection_doesNotSignalReselect() = runTest {
        val reselections = collectReselections()
        nav.switchTopLevel(settings)
        nav.switchTopLevel(home)
        assertTrue(reselections.isEmpty())
    }

    @Test
    fun goToDeepLink_inStartSection_showsItsBackStack() {
        val detail = TestKey("detail")
        assertTrue(nav.goToDeepLink(listOf(home, detail)))
        assertEquals(home, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, detail), nav.entries)
    }

    @Test
    fun goToDeepLink_inAnotherSection_selectsItAndBackExitsThroughHome() {
        val settingsDetail = TestKey("settings-detail")
        assertTrue(nav.goToDeepLink(listOf(settings, settingsDetail)))
        assertEquals(settings, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, settings, settingsDetail), nav.entries)

        // Back walks the link's own hierarchy, then falls back to the start section.
        nav.goBack()
        assertEquals<List<NavKey>>(listOf(home, settings), nav.entries)
        nav.goBack()
        assertEquals(home, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
    }

    @Test
    fun goToDeepLink_replacesWhatTheSectionShowedAboveTheSharedEntries() {
        // The section was showing home > a > edit; the link asks for home > a > b.
        val a = TestKey("a")
        nav.goTo(a)
        nav.goTo(TestKey("edit"))
        assertTrue(nav.goToDeepLink(listOf(home, a, TestKey("b"))))
        assertEquals<List<NavKey>>(listOf(home, a, TestKey("b")), homeStack.toList())
    }

    @Test
    fun goToDeepLink_toAShorterStack_dropsTheEntriesAboveIt() {
        nav.goTo(TestKey("a"))
        nav.goTo(TestKey("b"))
        assertTrue(nav.goToDeepLink(listOf(home)))
        assertEquals<List<NavKey>>(listOf(home), homeStack.toList())
    }

    @Test
    fun goToDeepLink_toTheStackOnShow_changesNothing() {
        // The same link opened twice must not stack a duplicate entry.
        val detail = TestKey("detail")
        val link = listOf<NavKey>(home, detail)
        assertTrue(nav.goToDeepLink(link))
        assertTrue(nav.goToDeepLink(link))
        assertEquals<List<NavKey>>(link, homeStack.toList())
    }

    @Test
    fun goToDeepLink_leavesEveryOtherSectionAlone() {
        // An edit in progress in the start section survives a link into settings.
        val edit = TestKey("edit")
        nav.goTo(edit)
        assertTrue(nav.goToDeepLink(listOf(settings, TestKey("settings-detail"))))
        assertEquals<List<NavKey>>(listOf(home, edit), homeStack.toList())

        nav.switchTopLevel(home)
        assertEquals<List<NavKey>>(listOf(home, edit), nav.entries)
    }

    @Test
    fun goToDeepLink_fromAnotherSection_switchesToTheLinkedOne() {
        nav.switchTopLevel(settings)
        nav.goTo(TestKey("settings-detail"))
        assertTrue(nav.goToDeepLink(listOf(home, TestKey("detail"))))
        assertEquals(home, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, TestKey("detail")), nav.entries)
        assertEquals<List<NavKey>>(listOf(settings, TestKey("settings-detail")), settingsStack.toList())
    }

    @Test
    fun goToDeepLink_whoseRootIsNotASection_isRefused() {
        nav.goTo(TestKey("detail"))
        assertFalse(nav.goToDeepLink(listOf(TestKey("orphan"), TestKey("child"))))
        assertEquals(home, nav.currentTopLevel)
        assertEquals<List<NavKey>>(listOf(home, TestKey("detail")), nav.entries)
    }

    @Test
    fun goToDeepLink_emptyBackStack_isRefused() {
        assertFalse(nav.goToDeepLink(emptyList()))
        assertEquals<List<NavKey>>(listOf(home), nav.entries)
    }

    @Test
    fun goToDeepLink_doesNotSignalReselect() = runTest {
        val reselections = collectReselections()
        assertTrue(nav.goToDeepLink(listOf(home, TestKey("detail"))))
        assertTrue(reselections.isEmpty())
    }

    /** Collects [Navigator.reselections] eagerly, so each signal is recorded as it is emitted. */
    private fun TestScope.collectReselections(): List<NavKey> {
        val received = mutableListOf<NavKey>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            nav.reselections.toList(received)
        }
        return received
    }
}
