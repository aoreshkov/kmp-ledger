package app.oreshkov.ledger.core.bootstrap.di

import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.navigation.DeepLinkResolver
import app.oreshkov.ledger.core.navigation.StartDestination
import app.oreshkov.ledger.core.ui.di.AppModule
import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import app.oreshkov.ledger.feature.posting.api.navigation.postingDeepLinks
import app.oreshkov.ledger.feature.posting.api.navigation.serializerPostings
import app.oreshkov.ledger.feature.posting.impl.di.PostingModule
import app.oreshkov.ledger.feature.settings.api.navigation.serializerSettings
import app.oreshkov.ledger.feature.settings.api.navigation.settingsDeepLinks
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

    @Single
    internal fun deepLinkResolver() = DeepLinkResolver(
        postingDeepLinks(DEEP_LINK_BASE_URI) + settingsDeepLinks(DEEP_LINK_BASE_URI),
    )
}

/**
 * The app's own link scheme. Each platform registers it separately (the Android manifest's
 * intent filter, the iOS `Info.plist`), so changing it here means changing it there too.
 */
internal const val DEEP_LINK_BASE_URI = "ledger://"
