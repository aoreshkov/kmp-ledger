package app.oreshkov.ledger.feature.settings.impl.di

import app.oreshkov.ledger.core.datastore.di.DataStoreModule
import app.oreshkov.ledger.core.domain.di.DomainModule
import app.oreshkov.ledger.core.navigation.LocalNavigator
import app.oreshkov.ledger.feature.settings.api.navigation.SettingsHome
import app.oreshkov.ledger.feature.settings.impl.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Module(includes = [DataStoreModule::class, DomainModule::class])
@ComponentScan("app.oreshkov.ledger.feature.settings.impl")
class SettingsModule

@OptIn(KoinExperimentalAPI::class)
val settingsNavigationModule = module {
    navigation<SettingsHome> {
        val navigator = LocalNavigator.current
        SettingsScreen(
            onNavigateBack = { navigator.goBack() },
            viewModel = koinViewModel()
        )
    }
}
