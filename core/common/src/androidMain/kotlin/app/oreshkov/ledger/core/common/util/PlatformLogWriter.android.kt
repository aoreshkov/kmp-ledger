package app.oreshkov.ledger.core.common.util

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.LogcatWriter

internal actual fun getPlatformLogWriters(): List<LogWriter> = listOf(LogcatWriter())