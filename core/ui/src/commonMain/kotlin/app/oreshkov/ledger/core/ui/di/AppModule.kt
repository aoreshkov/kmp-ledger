package app.oreshkov.ledger.core.ui.di

import app.oreshkov.ledger.core.common.di.LoggingModule
import org.koin.core.annotation.Module

@Module(includes = [LoggingModule::class])
class AppModule