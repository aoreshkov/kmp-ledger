package app.oreshkov.ledger.core.bootstrap

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

fun MainViewController(): UIViewController {
    startKoin<LedgerApp> {
        printLogger(Level.DEBUG)
        modules(postingNavigationModule)
    }
    return ComposeUIViewController {
        App()
    }
}