package app.oreshkov.kmpledger.core.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey

class Navigator(startDestination: NavKey) {
    private var _backStack by mutableStateOf<MutableList<NavKey>>(mutableStateListOf(startDestination))
    val backStack: List<NavKey> get() = _backStack

    fun bind(backStack: MutableList<NavKey>) {
        _backStack = backStack
    }

    fun goTo(destination: NavKey) {
        _backStack.add(destination)
    }

    fun canGoBack(): Boolean = _backStack.size > 1

    fun goBack() {
        if (canGoBack()) _backStack.removeLast()
    }
}