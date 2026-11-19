package app.oreshkov.ledger.feature.settings.api.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatchResult
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.withBackStack

/**
 * The links into this feature. `<base>settings` opens [SettingsHome], which is its own section
 * root, so the back stack is that single key.
 *
 * @param baseUri everything before the path, ending in its separator: `ledger://` for the
 *   app's own scheme, `https://example.com/` for web links.
 */
fun settingsDeepLinks(
    baseUri: String,
): List<DeepLinkMatcher<NavKey, BackStackMatchResult<NavKey, NavKey>>> = listOf(
    UriDeepLinkMatcher(DeepLinkUri("${baseUri}settings"), SettingsHome.serializer())
        .withBackStack<SettingsHome, NavKey> { match -> listOf(match.key) },
)
