package app.oreshkov.ledger.feature.posting.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Collects [flow] into the returned list on [dispatcher] — the same dispatcher the
 * ViewModel emits on — so advancing that dispatcher's scheduler drains the exact queue
 * the producer uses (catching a wrongly-sent one-shot event, not just a missing one).
 *
 * Launch this in `backgroundScope` so the never-completing collector is auto-cancelled at
 * test end and is not awaited by `runTest`. This is the official coroutines-test way to
 * assert presence *and absence* of emissions without a third-party library or relying on
 * a `first()` call timing out.
 */
fun <T> CoroutineScope.collectToList(
    flow: Flow<T>,
    dispatcher: CoroutineDispatcher,
): List<T> {
    val items = mutableListOf<T>()
    launch(dispatcher) { flow.collect { items += it } }
    return items
}
