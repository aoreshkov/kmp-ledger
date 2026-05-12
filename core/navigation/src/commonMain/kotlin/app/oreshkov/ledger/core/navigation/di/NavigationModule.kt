package app.oreshkov.ledger.core.navigation.di

import app.oreshkov.ledger.core.navigation.Navigator
import app.oreshkov.ledger.core.navigation.StartDestination
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
class NavigationModule {

    @Single
    fun provideNavigator(
        @Provided startDestination: StartDestination
    ): Navigator = Navigator(startDestination.key)
}