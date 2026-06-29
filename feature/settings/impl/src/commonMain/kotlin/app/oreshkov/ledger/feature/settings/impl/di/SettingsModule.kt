package app.oreshkov.ledger.feature.settings.impl.di

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import app.oreshkov.ledger.core.datastore.di.DataStoreModule
import app.oreshkov.ledger.core.domain.di.DomainModule
import app.oreshkov.ledger.core.navigation.LocalNavigator
import app.oreshkov.ledger.core.navigation.TopLevelDestination
import app.oreshkov.ledger.feature.settings.api.navigation.SettingsHome
import app.oreshkov.ledger.feature.settings.impl.SettingsScreen
import ledger.feature.settings.impl.generated.resources.Res
import ledger.feature.settings.impl.generated.resources.settings_nav_label
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Module(includes = [DataStoreModule::class, DomainModule::class])
@ComponentScan("app.oreshkov.ledger.feature.settings.impl")
class SettingsModule

@OptIn(KoinExperimentalAPI::class)
val settingsNavigationModule = module {
    // Contributes this feature as a top-level destination. A distinct qualifier keeps
    // it from overriding other features' destinations; the app aggregates via getAll().
    single(named("settings_top_level")) {
        TopLevelDestination(
            key = SettingsHome,
            label = Res.string.settings_nav_label,
            icon = Icons.Filled.Settings,
            order = 1,
        )
    }

    navigation<SettingsHome> {
        val navigator = LocalNavigator.current
        SettingsScreen(
            onNavigateBack = { navigator.goBack() },
            viewModel = koinViewModel()
        )
    }
}
