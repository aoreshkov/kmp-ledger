import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.oreshkov.ledger.core.bootstrap.di.BootstrapModule
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import org.koin.core.annotation.KoinApplication
import org.koin.core.logger.Level
import org.koin.plugin.module.dsl.startKoin

import java.awt.Dimension

@KoinApplication(modules = [BootstrapModule::class])
class LedgerApp

fun main() = application {
    startKoin<LedgerApp> {
        printLogger(Level.DEBUG)
        modules(postingNavigationModule)
    }
    Window(
        title = "ledger",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
    }
}