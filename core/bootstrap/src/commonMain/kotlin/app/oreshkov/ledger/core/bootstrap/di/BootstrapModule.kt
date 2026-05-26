package app.oreshkov.ledger.core.bootstrap.di

import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.navigation.StartDestination
import app.oreshkov.ledger.core.ui.di.AppModule
import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import app.oreshkov.ledger.feature.posting.api.navigation.serializerPostings
import app.oreshkov.ledger.feature.posting.impl.di.PostingModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Module(includes = [AppModule::class, PostingModule::class])
class BootstrapModule {
    @Single
    internal fun startDestination() = StartDestination(PostingList)

    @Single
    internal fun savedStateConfiguration() = SavedStateConfiguration {
        serializersModule = serializerPostings
    }
}