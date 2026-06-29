package app.oreshkov.ledger.core.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(private val backStack: MutableList<NavKey>) {
    fun goTo(destination: NavKey) {
        backStack.add(destination)
    }

    fun canGoBack(): Boolean = backStack.size > 1

    fun goBack() {
        if (canGoBack()) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    /**
     * Switches to a top-level section, resetting that section to its root (single-top).
     * Used by the top-level navigation chrome so re-selecting the current section while
     * drilled in returns to its start, and switching sections never stacks history.
     */
    fun switchTopLevel(destination: NavKey) {
        if (backStack.size == 1 && backStack.first() == destination) return
        backStack.clear()
        backStack.add(destination)
    }

    val entries: List<NavKey> get() = backStack.toList()
}