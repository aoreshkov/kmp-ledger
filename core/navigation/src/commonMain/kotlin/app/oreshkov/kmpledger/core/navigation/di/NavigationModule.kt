package app.oreshkov.kmpledger.core.navigation.di

import app.oreshkov.kmpledger.core.navigation.Navigator
import app.oreshkov.kmpledger.core.navigation.StartDestination
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