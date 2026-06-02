package app.oreshkov.ledger.core.bootstrap.di

import androidx.navigation3.runtime.NavKey
import app.oreshkov.ledger.feature.posting.impl.PostingDetailsViewModel
import app.oreshkov.ledger.feature.posting.impl.PostingEditViewModel
import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import org.koin.core.annotation.KoinExperimentalAPI
import kotlin.test.Test
import org.koin.test.verify.verify
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.definition

class KoinModuleVerificationTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun bootstrapModuleGraphIsComplete() {
        BootstrapModule().module().verify(
            extraTypes = listOf(NavKey::class),
            injections = injectedParameters(
                definition<PostingDetailsViewModel>(Long::class),
                definition<PostingEditViewModel>(Long::class),
                definition<Logger>(LoggerConfig::class),
            )
        )
    }
}