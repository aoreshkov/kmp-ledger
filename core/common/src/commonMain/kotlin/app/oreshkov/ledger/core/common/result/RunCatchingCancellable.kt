package app.oreshkov.ledger.core.common.result

import kotlinx.coroutines.CancellationException

/**
 * Like [runCatching], but rethrows [CancellationException] instead of capturing it
 * as a failure. Use this inside coroutines so structured-concurrency cancellation
 * stays cooperative — a cancelled coroutine must not have its cancellation turned
 * into an [Result.failure] and mapped to an error state on a screen being torn down.
 *
 * Marked `suspend` (per kotlinx.coroutines#1814) so it cannot be called from plain
 * blocking code, where swallowing a [CancellationException] would be a bug.
 */
suspend inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
