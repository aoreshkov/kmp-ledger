package app.oreshkov.ledger.core.common.result

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RunCatchingCancellableTest {

    @Test
    fun returnsSuccessWhenBlockSucceeds() = runTest {
        val result = runCatchingCancellable { 42 }
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun wrapsNonCancellationThrowableAsFailure() = runTest {
        val boom = RuntimeException("boom")
        val result = runCatchingCancellable { throw boom }
        assertTrue(result.isFailure)
        assertEquals(boom, result.exceptionOrNull())
    }

    @Test
    fun rethrowsCancellationExceptionInsteadOfCapturingIt() = runTest {
        assertFailsWith<CancellationException> {
            runCatchingCancellable { throw CancellationException("cancelled") }
        }
    }
}
