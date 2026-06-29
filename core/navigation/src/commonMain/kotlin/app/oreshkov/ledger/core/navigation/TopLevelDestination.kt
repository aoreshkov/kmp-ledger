package app.oreshkov.ledger.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

/**
 * A primary section of the app, surfaced in the top-level navigation chrome
 * (bottom bar / rail / drawer depending on window size).
 *
 * Each feature contributes its own [TopLevelDestination] via DI; the app-level
 * scaffold aggregates them and never references feature routes directly.
 *
 * @param key the section's start route (e.g. `PostingList`), used both to navigate
 *   to the section and to compute the selected item from the back-stack root.
 * @param order stable position of the item across features (ascending).
 */
data class TopLevelDestination(
    val key: NavKey,
    val label: StringResource,
    val icon: ImageVector,
    val order: Int,
)
