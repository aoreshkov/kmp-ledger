package app.oreshkov.ledger.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-level slot for the account/profile action rendered in a top-level screen's app bar
 * (e.g. `TopAppBar(actions = { LocalAccountAction.current() })`).
 *
 * Kept here — beside [LocalNavigator] — so feature screens can surface it without depending on
 * the auth feature. The app root provides the real button; the default renders nothing, so
 * screens, previews, and tests that don't provide it simply show no action.
 *
 * This is `staticCompositionLocalOf`, which does not track reads: changing the provided value
 * recomposes the *entire* content subtree under the provider. That is only cheap because the
 * provider supplies a capture-free lambda (`{ AccountButton() }`), which the Compose compiler
 * lifts to a stable singleton, so the value never actually changes across recompositions. Keep
 * the provided lambda capture-free. If it must capture state, either `remember` it at the
 * provider or switch this to `compositionLocalOf` to avoid recomposing the whole app.
 */
val LocalAccountAction = staticCompositionLocalOf<@Composable () -> Unit> { {} }
