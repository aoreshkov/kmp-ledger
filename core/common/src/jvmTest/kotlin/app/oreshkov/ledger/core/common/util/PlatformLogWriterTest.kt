package app.oreshkov.ledger.core.common.util

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import co.touchlab.kermit.Severity
import org.slf4j.LoggerFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The JVM `actual` writes through SLF4J, so the assertions go through logback — the binding
 * already on this source set's runtime classpath — rather than a fake. `ListAppender` attached
 * to one named logger keeps the capture scoped to this test.
 */
class PlatformLogWriterTest {

    private val tag = "PlatformLogWriterTest"
    private lateinit var logger: Logger
    private lateinit var appender: ListAppender<ILoggingEvent>

    @BeforeTest
    fun setUp() {
        logger = LoggerFactory.getLogger(tag) as Logger
        // The writer emits at debug for Verbose/Debug; logback's default root level would
        // swallow those on some configurations.
        logger.level = Level.TRACE
        appender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(appender)
    }

    @AfterTest
    fun tearDown() {
        logger.detachAppender(appender)
        appender.stop()
    }

    @Test
    fun providesExactlyOneWriter() {
        assertEquals(1, getPlatformLogWriters().size)
    }

    @Test
    fun mapsEverySeverityOntoAnSlf4jLevel() {
        val writer = getPlatformLogWriters().single()

        Severity.entries.forEach { severity ->
            writer.log(severity, "message", tag, null)
        }

        assertEquals(
            listOf(Level.DEBUG, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.ERROR),
            appender.list.map { it.level },
        )
    }

    @Test
    fun forwardsMessageAndThrowable() {
        val cause = IllegalStateException("boom")

        getPlatformLogWriters().single().log(Severity.Error, "message", tag, cause)

        val event = appender.list.single()
        assertEquals("message", event.message)
        assertEquals(tag, event.loggerName)
        assertEquals("boom", event.throwableProxy?.message)
    }
}
