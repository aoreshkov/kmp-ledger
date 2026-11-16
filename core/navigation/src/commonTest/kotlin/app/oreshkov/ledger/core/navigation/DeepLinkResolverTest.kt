package app.oreshkov.ledger.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.withBackStack
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Extends [PlatformComposeUiTest] only for its Android actual, which runs the class under
 * Robolectric: `DeepLinkUri` is `android.net.Uri` there, a stub on the plain host classpath. The
 * other targets use Navigation 3's own parser, so the same cases cover both implementations.
 */
class DeepLinkResolverTest : PlatformComposeUiTest() {

    @Serializable
    private data object Home : NavKey

    @Serializable
    private data object Featured : NavKey

    @Serializable
    private data class Item(val id: String) : NavKey

    private val resolver = DeepLinkResolver(
        listOf(
            UriDeepLinkMatcher(DeepLinkUri("test://items"), Home.serializer())
                .withBackStack<Home, NavKey> { match -> listOf(match.key) },
            UriDeepLinkMatcher(DeepLinkUri("test://items/{id}"), Item.serializer())
                .withBackStack<Item, NavKey> { match -> listOf(Home, match.key) },
            UriDeepLinkMatcher(DeepLinkUri("test://items/featured"), Featured.serializer())
                .withBackStack<Featured, NavKey> { match -> listOf(Home, match.key) },
        ),
    )

    @Test
    fun resolve_returnsTheMatchersBackStack() {
        assertEquals<List<NavKey>?>(listOf(Home), resolver.resolve("test://items"))
        assertEquals<List<NavKey>?>(listOf(Home, Item("42")), resolver.resolve("test://items/42"))
    }

    @Test
    fun resolve_exactPathOutranksPlaceholder() {
        // "featured" also satisfies {id}; declaration order must not decide the winner.
        assertEquals<List<NavKey>?>(listOf(Home, Featured), resolver.resolve("test://items/featured"))
    }

    @Test
    fun resolve_decodesAnEscapedArgument() {
        assertEquals<List<NavKey>?>(listOf(Home, Item("a b/c")), resolver.resolve("test://items/a%20b%2Fc"))
    }

    @Test
    fun resolve_ignoresQueryAndFragmentNoMatcherDeclares() {
        assertEquals<List<NavKey>?>(listOf(Home, Item("42")), resolver.resolve("test://items/42?utm=x#top"))
    }

    @Test
    fun resolve_unclaimedOrMalformedLink_returnsNull() {
        val unclaimed = listOf(
            "test://unknown",
            "test://items/42/extra",
            "other://items/42",
            "https://items/42",
            "mailto:someone@example.com",
            "test:items/42",
            "items/42",
            "not a uri",
            "",
        )
        for (link in unclaimed) {
            assertNull(resolver.resolve(link), "expected no match for \"$link\"")
        }
    }

    @Test
    fun resolve_withNoMatchers_returnsNull() {
        assertNull(DeepLinkResolver(emptyList()).resolve("test://items"))
    }
}
