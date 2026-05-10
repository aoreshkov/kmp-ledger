package app.oreshkov.kmpledger.core.ui.di

import app.oreshkov.kmpledger.core.navigation.di.NavigationModule
import org.koin.core.annotation.Module

@Module(includes = [NavigationModule::class])
class AppModule