package app.oreshkov.ledger.core.bootstrap.di

import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.navigation.StartDestination
import app.oreshkov.ledger.core.ui.di.AppModule
import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import app.oreshkov.ledger.feature.posting.api.navigation.serializerPostings
import app.oreshkov.ledger.feature.posting.impl.di.PostingModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AppModule::class, PostingModule::class])
internal class BootstrapModule {
    @Single
    fun startDestination() = StartDestination(PostingList)

    @Single
    fun savedStateConfiguration() = SavedStateConfiguration {
        serializersModule = serializerPostings
    }
}