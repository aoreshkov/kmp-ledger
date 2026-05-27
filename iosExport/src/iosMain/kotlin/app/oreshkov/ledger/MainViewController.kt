package app.oreshkov.ledger

import androidx.compose.ui.window.ComposeUIViewController
import app.oreshkov.ledger.core.bootstrap.di.BootstrapModule
import app.oreshkov.ledger.core.ui.App
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@KoinApplication(modules = [BootstrapModule::class])
internal class LedgerApp

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "MainViewController")
fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "initializeKoin")
fun initializeKoin() {
    startKoin<LedgerApp> {
        modules(postingNavigationModule)
    }
}