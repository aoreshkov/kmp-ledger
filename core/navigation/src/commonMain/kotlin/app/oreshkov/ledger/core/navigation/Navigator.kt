package app.oreshkov.ledger.core.navigation

import androidx.compose.runtime.MutableState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Holds one [NavBackStack] per top-level section (keyed by the section's start route) and tracks the
 * currently selected section. Switching sections preserves each section's own stack; only [startRoute]
 * and the current section are ever displayed (exit-through-home).
 */
class Navigator(
    val startRoute: NavKey,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    private val currentTopLevelState: MutableState<NavKey>,
) {
    var currentTopLevel: NavKey
        get() = currentTopLevelState.value
        private set(value) {
            currentTopLevelState.value = value
        }

    private val currentStack: NavBackStack<NavKey>
        get() = backStacks.getValue(currentTopLevel)

    /**
     * Drill into the current section; a no-op while [destination] is already the top entry. A repeat
     * tap would otherwise push a second entry with the same content key, sharing the first one's
     * ViewModel and saved state, so the user must press back twice. In the two-pane list-detail
     * layout the scene never transitions, so a lifecycle guard such as `dropUnlessResumed` cannot
     * catch this; only the back stack can.
     */
    fun goTo(destination: NavKey) {
        if (currentStack.lastOrNull() != destination) currentStack.add(destination)
    }

    fun canGoBack(): Boolean = currentStack.size > 1 || currentTopLevel != startRoute

    /** Pop within the current section; at a section root, fall back to the start section. */
    fun goBack() {
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
        } else if (currentTopLevel != startRoute) {
            currentTopLevel = startRoute
        }
    }

    /**
     * [goBack] on behalf of the screen showing [from], and only while it is the current top entry.
     * A screen's second request (a double tap during the exit transition, or a tap racing a
     * state-driven navigation) then finds another entry on top and does nothing, instead of
     * popping the screen beneath. System back keeps the unkeyed [goBack].
     */
    fun goBack(from: NavKey) {
        if (currentStack.lastOrNull() == from) goBack()
    }

    private val _reselections = MutableSharedFlow<NavKey>(extraBufferCapacity = 1)

    /**
     * Section roots re-selected in the navigation bar, for the screen on show to scroll to its top.
     * Unlike a ViewModel outcome this is meant only for whatever is composed at that moment, so a
     * signal with no collector, or one that overflows the buffer, is dropped rather than replayed.
     */
    val reselections: SharedFlow<NavKey> = _reselections.asSharedFlow()

    /**
     * Selects a top-level section, preserving that section's back stack. Re-selecting the section
     * that is already current leaves its stack alone and signals [reselections] instead, as the
     * Navigation 3 multiple-back-stacks recipe does: resetting to the root would turn a double tap
     * on another section into "switch, then discard the stack the switch just restored", unsaved
     * edits included.
     */
    fun switchTopLevel(destination: NavKey) {
        if (currentTopLevel == destination) {
            _reselections.tryEmit(destination)
        } else {
            currentTopLevel = destination
        }
    }

    /** Active concatenation used for back handling / assertions: start section then current section. */
    val entries: List<NavKey>
        get() = if (currentTopLevel == startRoute) {
            backStacks.getValue(startRoute).toList()
        } else {
            backStacks.getValue(startRoute).toList() + currentStack.toList()
        }
}
