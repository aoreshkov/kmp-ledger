package app.oreshkov.ledger.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatchResult
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest

/**
 * A deep link a feature supports. Matching one yields the synthetic back stack to show, whose
 * first key is the root of the section the link belongs to (see [Navigator.goToDeepLink]).
 */
typealias BackStackDeepLinkMatcher = DeepLinkMatcher<NavKey, BackStackMatchResult<NavKey, NavKey>>

/**
 * Resolves an incoming link against every feature's [matchers]. Each feature declares its own
 * next to its NavKeys; the root module combines them, as it does the NavKey serializers.
 */
class DeepLinkResolver(private val matchers: List<BackStackDeepLinkMatcher>) {

    /**
     * The back stack for [uri], or `null` when no matcher claims it. A link is untrusted input
     * from outside the app, so an unknown or malformed one is an expected outcome, not an error.
     *
     * When several matchers claim the link the most specific wins: an exact path over one with
     * placeholders, then the one that extracted more arguments.
     */
    fun resolve(uri: String): List<NavKey>? {
        val request = DeepLinkRequest(uri)
        return matchers.mapNotNull { it.match(request) }.maxOrNull()?.backStack
    }
}
