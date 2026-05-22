package app.oreshkov.ledger.core.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(val backStack: MutableList<NavKey>) {
    fun goTo(destination: NavKey) {
        backStack.add(destination)
    }

    fun canGoBack(): Boolean = backStack.size > 1

    fun goBack() {
        if (canGoBack()) {
            backStack.removeAt(backStack.size - 1)
        }
    }
}