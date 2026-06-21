package app.oreshkov.ledger.core.common.dispatcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.test.Test
import kotlin.test.assertSame

class AppDispatchersTest {

    @Test
    fun defaultAppDispatchers_io_isDispatchersIO() {
        assertSame(Dispatchers.IO, DefaultAppDispatchers().io)
    }

    @Test
    fun defaultAppDispatchers_default_isDispatchersDefault() {
        assertSame(Dispatchers.Default, DefaultAppDispatchers().default)
    }
}
