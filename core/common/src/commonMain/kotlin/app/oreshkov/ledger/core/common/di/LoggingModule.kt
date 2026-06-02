package app.oreshkov.ledger.core.common.di

import app.oreshkov.ledger.core.common.util.getPlatformLogWriters
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class LoggingModule {
    @Single
    fun logger() = Logger(
        config = StaticConfig(
            logWriterList = getPlatformLogWriters()
        ),
        tag = "KMP-Ledger"
    )
}