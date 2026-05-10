package app.oreshkov.kmpledger.core.bootstrap.di

import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.kmpledger.core.navigation.StartDestination
import app.oreshkov.kmpledger.core.ui.di.AppModule
import app.oreshkov.kmpledger.feature.posting.api.navigation.PostingList
import app.oreshkov.kmpledger.feature.posting.api.navigation.serializerPostings
import app.oreshkov.kmpledger.feature.posting.impl.di.PostingModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AppModule::class, PostingModule::class])
class BootstrapModule {
    @Single
    fun startDestination() = StartDestination(PostingList)

    @Single
    fun savedStateConfiguration() = SavedStateConfiguration {
        serializersModule = serializerPostings
    }
}