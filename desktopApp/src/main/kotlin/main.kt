package app.oreshkov.kmpledger

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.oreshkov.kmpledger.core.bootstrap.di.BootstrapModule
import app.oreshkov.kmpledger.core.ui.App
import app.oreshkov.kmpledger.feature.posting.impl.di.postingNavigationModule
import org.koin.core.annotation.KoinApplication
import org.koin.core.logger.Level
import org.koin.plugin.module.dsl.startKoin

import java.awt.Dimension

@KoinApplication(modules = [BootstrapModule::class])
class KMPLedgerApp

fun main() = application {
    startKoin<KMPLedgerApp> {
        printLogger(Level.DEBUG)
        modules(postingNavigationModule)
    }
    Window(
        title = "kmpledger",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
    }
}