package app.oreshkov.ledger.core.common.result

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DataResultTest {

    @Test
    fun asResult_emitsLoadingFirst() = runTest {
        val result = flow { emit(42) }.asResult().toList()
        assertIs<DataResult.Loading>(result.first())
    }

    @Test
    fun asResult_emitsSuccessWithValue() = runTest {
        val results = flow { emit("hello") }.asResult().toList()
        val success = results.last()
        assertIs<DataResult.Success<String>>(success)
        assertEquals("hello", success.data)
    }

    @Test
    fun asResult_emitsErrorOnException() = runTest {
        val boom = RuntimeException("boom")
        val results = flow<Int> { throw boom }.asResult().toList()
        // Loading then Error
        assertEquals(2, results.size)
        val error = results.last()
        assertIs<DataResult.Error>(error)
        assertEquals(boom, error.exception)
    }
}