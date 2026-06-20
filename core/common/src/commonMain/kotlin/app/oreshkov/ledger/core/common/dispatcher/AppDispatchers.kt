package app.oreshkov.ledger.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Injectable seam over the coroutine dispatchers used by lower layers.
 *
 * Production code resolves [DefaultAppDispatchers] via Koin; tests can supply a
 * deterministic implementation (e.g. backed by a `TestDispatcher`) without leaning
 * on the platform-specific semantics of [Dispatchers.IO].
 */
interface AppDispatchers {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultAppDispatchers : AppDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
