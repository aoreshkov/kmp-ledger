package app.oreshkov.kmpledger.core.domain.di

import app.oreshkov.kmpledger.core.data.di.DataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DataModule::class])
@ComponentScan("app.oreshkov.kmpledger.core.domain")
class DomainModule