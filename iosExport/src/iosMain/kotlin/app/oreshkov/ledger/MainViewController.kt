package app.oreshkov.ledger

import androidx.compose.ui.window.ComposeUIViewController
import app.oreshkov.ledger.core.bootstrap.di.BootstrapModule
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import org.koin.core.annotation.KoinApplication
import org.koin.core.logger.Level
import org.koin.plugin.module.dsl.startKoin
import platform.UIKit.UIViewController

@KoinApplication(modules = [BootstrapModule::class])
internal class LedgerApp

public fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}

public fun initializeKoin() {
    startKoin<LedgerApp> {
        printLogger(Level.DEBUG)
        modules(postingNavigationModule)
    }
}