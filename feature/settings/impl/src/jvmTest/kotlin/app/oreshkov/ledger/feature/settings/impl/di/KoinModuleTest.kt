package app.oreshkov.ledger.feature.settings.impl.di

import org.koin.core.annotation.KoinExperimentalAPI
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
        settingsNavigationModule.verify()
    }
}
