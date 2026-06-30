package app.oreshkov.ledger.core.navigation

import androidx.compose.runtime.MutableState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

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

    /** Drill into the current section. */
    fun goTo(destination: NavKey) {
        currentStack.add(destination)
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
     * Selects a top-level section, preserving that section's back stack. Re-selecting the section
     * that is already current (while drilled in) resets it to its root.
     */
    fun switchTopLevel(destination: NavKey) {
        if (currentTopLevel == destination) {
            val stack = backStacks.getValue(destination)
            while (stack.size > 1) stack.removeAt(stack.lastIndex)
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
