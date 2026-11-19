package app.oreshkov.ledger.feature.posting.api.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The link patterns are public contract in the same way the NavKeys are: a link shared today
 * has to open the same screen after the next release.
 *
 * JVM only. On the Android host `DeepLinkUri` is `android.net.Uri`, a stub without Robolectric,
 * which this module does not carry; `core:bootstrap` runs the combined table on every target.
 */
class PostingDeepLinksTest {

    private val matchers = postingDeepLinks("ledger://")

    private fun resolve(uri: String): List<NavKey>? {
        val request = DeepLinkRequest(uri)
        return matchers.mapNotNull { it.match(request) }.maxOrNull()?.backStack
    }

    @Test
    fun postings_opensTheList() {
        assertEquals<List<NavKey>?>(listOf(PostingList), resolve("ledger://postings"))
    }

    @Test
    fun postingId_opensDetailsAboveTheList() {
        assertEquals<List<NavKey>?>(
            listOf(PostingList, PostingDetail("42")),
            resolve("ledger://postings/42"),
        )
    }

    @Test
    fun postingIdEdit_opensTheEditorAboveItsDetails() {
        assertEquals<List<NavKey>?>(
            listOf(PostingList, PostingDetail("42"), PostingEdit("42")),
            resolve("ledger://postings/42/edit"),
        )
    }

    @Test
    fun postingsNew_opensTheAddScreen_notDetailsOfAPostingCalledNew() {
        assertEquals<List<NavKey>?>(
            listOf(PostingList, PostingEdit(null)),
            resolve("ledger://postings/new"),
        )
    }

    @Test
    fun escapedId_isDecoded() {
        assertEquals<List<NavKey>?>(
            listOf(PostingList, PostingDetail("a b")),
            resolve("ledger://postings/a%20b"),
        )
    }

    @Test
    fun schemeAndHost_areCaseInsensitive() {
        assertEquals<List<NavKey>?>(
            listOf(PostingList, PostingDetail("42")),
            resolve("LEDGER://Postings/42"),
        )
    }

    @Test
    fun otherLinks_areNotClaimed() {
        val unclaimed = listOf(
            "ledger://postings/42/delete",
            "ledger://postings/42/edit/extra",
            "ledger://posting/42",
            "ledger://settings",
            "https://postings/42",
        )
        for (link in unclaimed) {
            assertNull(resolve(link), "expected no match for \"$link\"")
        }
    }

    @Test
    fun baseUri_selectsTheSchemeAndHost() {
        val web = postingDeepLinks("https://example.com/")
        val request = DeepLinkRequest("https://example.com/postings/42")
        assertEquals<List<NavKey>?>(
            listOf(PostingList, PostingDetail("42")),
            web.mapNotNull { it.match(request) }.maxOrNull()?.backStack,
        )
        assertNull(resolve("https://example.com/postings/42"))
    }
}
