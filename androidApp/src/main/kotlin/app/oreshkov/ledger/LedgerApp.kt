package app.oreshkov.ledger

import android.app.Application
import app.oreshkov.ledger.core.bootstrap.di.BootstrapModule
import app.oreshkov.ledger.feature.posting.impl.di.postingNavigationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [BootstrapModule::class])
class LedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<LedgerApp> {
            androidContext(this@LedgerApp)
            modules(postingNavigationModule)
        }
    }
}