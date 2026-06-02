package app.oreshkov.ledger.core.ui.di

import app.oreshkov.ledger.core.common.di.LoggingModule
import app.oreshkov.ledger.core.navigation.di.NavigationModule
import org.koin.core.annotation.Module

@Module(includes = [NavigationModule::class, LoggingModule::class])
class AppModule