package app.oreshkov.ledger.core.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.domain.GetThemeModeUseCase
import app.oreshkov.ledger.core.model.settings.ThemeMode
import app.oreshkov.ledger.core.navigation.LocalNavigator
import app.oreshkov.ledger.core.navigation.Navigator
import app.oreshkov.ledger.core.navigation.StartDestination
import app.oreshkov.ledger.core.navigation.TopLevelDestination
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

import app.oreshkov.ledger.core.ui.theme.LedgerTheme

@OptIn(
    KoinExperimentalAPI::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
)
@Composable
fun App() {
    // Until the stored preference loads, fall back to the OS setting (ThemeMode.SYSTEM).
    // DataStore reads are async, so a cold start may briefly show the system theme.
    val getThemeMode = koinInject<GetThemeModeUseCase>()
    val themeFlow = remember(getThemeMode) { getThemeMode() }
    val themeMode by themeFlow.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    LedgerTheme(themeMode = themeMode) {
        Surface {
            val startDestination = koinInject<StartDestination>()
            val savedStateConfiguration = koinInject<SavedStateConfiguration>()

            // Each feature contributes its TopLevelDestination via Koin; the app-level
            // scaffold aggregates them and stays feature-agnostic (see TopLevelDestination).
            // Aggregated once and remembered -> stable order, so calling remember*/
            // rememberNavBackStack per element below is a legal Compose loop.
            val koin = getKoin()
            val topLevelDestinations = remember(koin) {
                koin.getAll<TopLevelDestination>().sortedBy { it.order }
            }

            // Section roots that own a back stack: the start route plus every top-level
            // destination. The start route is included explicitly so it always has a stack
            // even when it is not itself surfaced as a top-level destination.
            val sectionRoots = remember(topLevelDestinations, startDestination) {
                (listOf(startDestination.key) + topLevelDestinations.map { it.key }).distinct()
            }

            // One persistent back stack per section, keyed by section root. Each survives
            // configuration change and process death via rememberNavBackStack.
            val backStacks = sectionRoots.associateWith { root ->
                rememberNavBackStack(savedStateConfiguration, root)
            }

            // Selected section. Plain remember: the section stacks themselves are restored
            // across process death; only the selected tab resets to start, an acceptable root.
            val currentTopLevel = remember { mutableStateOf<NavKey>(startDestination.key) }

            val navigator = remember(backStacks) {
                Navigator(
                    startRoute = startDestination.key,
                    backStacks = backStacks,
                    currentTopLevelState = currentTopLevel,
                )
            }

            val entryProvider = koinEntryProvider<NavKey>()
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
            val sceneStrategies = remember(listDetailStrategy) { listOf(listDetailStrategy) }

            // Decorate EVERY section every frame so inactive sections keep their ViewModels
            // and saved UI state alive; each section gets its own decorator instances so
            // per-stack disposal stays isolated (shared instances would treat the other
            // sections' entries as removed and dispose them).
            val decoratedBySection: Map<NavKey, List<NavEntry<NavKey>>> =
                sectionRoots.associateWith { root ->
                    val saveableStateDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
                    val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
                    val decorators = remember(saveableStateDecorator, viewModelStoreDecorator) {
                        listOf(saveableStateDecorator, viewModelStoreDecorator)
                    }
                    rememberDecoratedNavEntries(
                        backStack = backStacks.getValue(root),
                        entryDecorators = decorators,
                        entryProvider = entryProvider,
                    )
                }

            // Display only the start section then the current section (exit-through-home).
            val displayedEntries = remember(decoratedBySection, navigator.currentTopLevel) {
                buildList {
                    addAll(decoratedBySection.getValue(navigator.startRoute))
                    if (navigator.currentTopLevel != navigator.startRoute) {
                        addAll(decoratedBySection.getValue(navigator.currentTopLevel))
                    }
                }
            }

            CompositionLocalProvider(LocalNavigator provides navigator) {
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        topLevelDestinations.forEach { destination ->
                            item(
                                selected = navigator.currentTopLevel == destination.key,
                                onClick = { navigator.switchTopLevel(destination.key) },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(stringResource(destination.label)) },
                            )
                        }
                    }
                ) {
                    NavDisplay(
                        entries = displayedEntries,
                        onBack = { navigator.goBack() },
                        sceneStrategies = sceneStrategies,
                    )
                }
            }
        }
    }
}