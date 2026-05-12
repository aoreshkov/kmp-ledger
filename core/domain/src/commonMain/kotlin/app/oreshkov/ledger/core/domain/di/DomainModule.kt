package app.oreshkov.ledger.core.domain.di

import app.oreshkov.ledger.core.data.di.DataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DataModule::class])
@ComponentScan("app.oreshkov.ledger.core.domain")
class DomainModule