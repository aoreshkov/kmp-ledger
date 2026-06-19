package app.oreshkov.ledger.feature.posting.impl.di

import app.oreshkov.ledger.feature.posting.impl.PostingDetailsViewModel
import app.oreshkov.ledger.feature.posting.impl.PostingEditViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import kotlin.test.Test

class KoinModuleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyPostingModule() {
        PostingModule().module().verify(
            injections = injectedParameters(
                definition<PostingDetailsViewModel>(String::class),
                definition<PostingEditViewModel>(String::class),
            )
        )
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyPostingNavigationModule() {
        postingNavigationModule.verify()
    }
}