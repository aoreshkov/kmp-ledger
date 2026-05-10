package app.oreshkov.kmpledger

import android.app.Application
import app.oreshkov.kmpledger.core.bootstrap.di.BootstrapModule
import app.oreshkov.kmpledger.feature.posting.impl.di.postingNavigationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [BootstrapModule::class])
class KMPLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<KMPLedgerApp> {
            androidContext(this@KMPLedgerApp)
            modules(postingNavigationModule)
        }
    }
}