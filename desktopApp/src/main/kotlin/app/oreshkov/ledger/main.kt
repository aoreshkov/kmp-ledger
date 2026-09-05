package app.oreshkov.ledger

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.v2.Window
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import app.oreshkov.ledger.core.bootstrap.di.BootstrapModule
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import app.oreshkov.ledger.feature.settings.impl.di.settingsNavigationModule
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [BootstrapModule::class])
class LedgerApp

// Window API v2 (Compose Multiplatform 1.12.0). Experimental, and its KDoc notes the package
// may move to androidx.compose.ui.window before stabilization — expect an import change, not a
// rewrite. It is used here because `minSize` is typed in Dp: the v1 form had to reach through
// to AWT (`window.minimumSize = Dimension(350, 600)`), whose device pixels do not scale with
// display density, so the floor was half the intended physical size at 200% while the Dp-typed
// initial size scaled correctly.
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin<LedgerApp> {
        modules(postingNavigationModule, settingsNavigationModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Ledger",
            state = rememberWindowState(
                initialBoundsProvider = WindowBoundsProvider(
                    sizeProvider = WindowSizeProvider.Fixed(DpSize(800.dp, 600.dp)),
                    // The v1 default cascades new windows from the last position, which on a
                    // multi-monitor setup lands wherever the previous window was.
                    positionProvider = WindowPositionProvider.CenteredOnScreen,
                ),
            ),
            minSize = DpSize(350.dp, 600.dp),
        ) {
            App()
        }
    }
}
