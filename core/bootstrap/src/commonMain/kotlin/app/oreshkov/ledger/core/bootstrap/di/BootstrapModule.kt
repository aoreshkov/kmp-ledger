package app.oreshkov.ledger.core.bootstrap.di

import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.navigation.StartDestination
import app.oreshkov.ledger.core.ui.di.AppModule
import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import app.oreshkov.ledger.feature.posting.api.navigation.serializerPostings
import app.oreshkov.ledger.feature.posting.impl.di.PostingModule
import app.oreshkov.ledger.feature.settings.api.navigation.serializerSettings
import app.oreshkov.ledger.feature.settings.impl.di.SettingsModule
import kotlinx.serialization.modules.plus
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AppModule::class, PostingModule::class, SettingsModule::class])
class BootstrapModule {
    @Single
    internal fun startDestination() = StartDestination(PostingList)

    @Single
    internal fun savedStateConfiguration() = SavedStateConfiguration {
        serializersModule = serializerPostings + serializerSettings
    }
}
