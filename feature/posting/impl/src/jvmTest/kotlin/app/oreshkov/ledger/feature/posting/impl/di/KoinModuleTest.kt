package app.oreshkov.ledger.feature.posting.impl.di

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import app.oreshkov.ledger.core.navigation.TopLevelDestination
import app.oreshkov.ledger.feature.posting.impl.PostingDetailsViewModel
import app.oreshkov.ledger.feature.posting.impl.PostingEditViewModel
import org.jetbrains.compose.resources.StringResource
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
        // TopLevelDestination is built from literal values in the module lambda, not
        // resolved from the container; tell verify those constructor args are supplied.
        postingNavigationModule.verify(
            injections = injectedParameters(
                definition<TopLevelDestination>(
                    NavKey::class,
                    StringResource::class,
                    ImageVector::class,
                    Int::class,
                )
            )
        )
    }
}