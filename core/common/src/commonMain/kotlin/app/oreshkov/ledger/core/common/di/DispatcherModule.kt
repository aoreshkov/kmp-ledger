package app.oreshkov.ledger.core.common.di

import app.oreshkov.ledger.core.common.dispatcher.AppDispatchers
import app.oreshkov.ledger.core.common.dispatcher.DefaultAppDispatchers
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DispatcherModule {
    @Single
    fun provideAppDispatchers(): AppDispatchers = DefaultAppDispatchers()
}
