package app.oreshkov.ledger.feature.settings.impl.di

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import app.oreshkov.ledger.core.navigation.TopLevelDestination
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import kotlin.test.Test

class KoinModuleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifySettingsModule() {
        SettingsModule().module().verify()
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifySettingsNavigationModule() {
        // TopLevelDestination is built from literal values in the module lambda, not
        // resolved from the container; tell verify those constructor args are supplied.
        settingsNavigationModule.verify(
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
