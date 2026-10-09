package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Hosts [content] the way `NavDisplay` hosts a screen during a scene transition: at `STARTED`, so
 * `dropUnlessResumed` clicks must be dropped. The test host itself is `RESUMED`.
 *
 * Uses [LifecycleRegistry.createUnsafe] rather than `rememberLifecycleOwner`: on desktop the
 * registry's main-thread check does `runBlocking(Dispatchers.Main)`, which deadlocks in test
 * classes that swap Main for a `StandardTestDispatcher`.
 */
@Composable
internal fun NotResumed(content: @Composable () -> Unit) {
    val owner = remember { StartedLifecycleOwner() }
    CompositionLocalProvider(LocalLifecycleOwner provides owner, content = content)
}

private class StartedLifecycleOwner : LifecycleOwner {
    override val lifecycle: LifecycleRegistry =
        LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.STARTED }
}
