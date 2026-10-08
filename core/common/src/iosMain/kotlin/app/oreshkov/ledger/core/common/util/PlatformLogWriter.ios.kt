package app.oreshkov.ledger.core.common.util

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.OSLogWriter

internal actual fun getPlatformLogWriters(): List<LogWriter> = listOf(OSLogWriter())