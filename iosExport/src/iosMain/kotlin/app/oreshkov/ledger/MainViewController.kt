package app.oreshkov.ledger

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import app.oreshkov.ledger.core.bootstrap.di.BootstrapModule
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import app.oreshkov.ledger.feature.settings.impl.di.settingsNavigationModule
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin
import platform.UIKit.UIViewController

@KoinApplication(modules = [BootstrapModule::class])
internal class LedgerApp

/** A link SwiftUI handed over through [openDeepLink] and `App()` has not yet acknowledged. */
private var pendingDeepLink by mutableStateOf<String?>(null)

fun MainViewController(): UIViewController = ComposeUIViewController {
    App(
        deepLink = pendingDeepLink,
        onDeepLinkConsumed = { pendingDeepLink = null },
    )
}

fun initializeKoin() {
    startKoin<LedgerApp> {
        modules(postingNavigationModule, settingsNavigationModule)
    }
}

/**
 * Entry point for SwiftUI's `.onOpenURL`. Safe to call before [MainViewController] exists: the
 * link is held as state until the first composition picks it up.
 */
fun openDeepLink(url: String) {
    pendingDeepLink = url
}
