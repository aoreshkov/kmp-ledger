package app.oreshkov.ledger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.oreshkov.ledger.core.ui.App

class MainActivity : ComponentActivity() {

    /** A link this activity received and `App()` has not yet acknowledged. */
    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only on a fresh start. After a rotation or a process death `intent` is still the
        // one that started the activity, and applying its link again would overwrite the
        // back stacks that were just restored.
        if (savedInstanceState == null) pendingDeepLink = intent.deepLink()
        setContent {
            App(
                deepLink = pendingDeepLink,
                onDeepLinkConsumed = { pendingDeepLink = null },
            )
        }
    }

    // launchMode="singleTop": a link opened while this activity is on top of its task is
    // delivered here instead of starting a second copy of the app above the first.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.deepLink()?.let { pendingDeepLink = it }
    }
}

/**
 * The link carried by a `VIEW` intent. An intent replayed from Recents is skipped: after the
 * user has backed out of the app, reopening it there re-delivers the original intent, and the
 * link would otherwise fire a second time.
 */
private fun Intent.deepLink(): String? {
    val launchedFromHistory = (flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0
    return if (action == Intent.ACTION_VIEW && !launchedFromHistory) dataString else null
}
