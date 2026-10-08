package app.oreshkov.ledger.core.common.util

import co.touchlab.kermit.LogWriter

internal expect fun getPlatformLogWriters(): List<LogWriter>