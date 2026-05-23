package app.oreshkov.ledger.core.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.navigation.LocalNavigator
import app.oreshkov.ledger.core.navigation.Navigator
import app.oreshkov.ledger.core.navigation.StartDestination
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

import app.oreshkov.ledger.core.ui.theme.LedgerTheme

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App() {
    LedgerTheme {
        Surface {
            val startDestination = koinInject<StartDestination>()
            val savedStateConfiguration = koinInject<SavedStateConfiguration>()

            val backStack = rememberNavBackStack(
                savedStateConfiguration,
                startDestination.key
            )

            val navigator = remember(backStack) { Navigator(backStack) }

            val entryProvider = koinEntryProvider<NavKey>()
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
            
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator<NavKey>()
            )

            CompositionLocalProvider(LocalNavigator provides navigator) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { navigator.goBack() },
                    sceneStrategies = listOf(listDetailStrategy),
                    entryProvider = entryProvider,
                    entryDecorators = decorators
                )
            }
        }
    }
}