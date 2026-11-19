package app.oreshkov.ledger.core.bootstrap.di

import androidx.navigation3.runtime.NavKey
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import app.oreshkov.ledger.feature.posting.api.navigation.PostingDetail
import app.oreshkov.ledger.feature.posting.api.navigation.PostingEdit
import app.oreshkov.ledger.feature.posting.api.navigation.PostingList
import app.oreshkov.ledger.feature.settings.api.navigation.SettingsHome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the *combined* link table the app ships, under the scheme the platforms register. The
 * per-feature tests each check their own matchers alone, on the JVM; a feature left out of
 * [BootstrapModule.deepLinkResolver] would otherwise only show up as a link that does nothing.
 *
 * Extends [PlatformComposeUiTest] only for its Android actual, which runs the class under
 * Robolectric: `DeepLinkUri` is `android.net.Uri` there. So this is also where the real links
 * are parsed by each platform's own URI implementation.
 */
class BootstrapDeepLinksTest : PlatformComposeUiTest() {

    private val resolver = BootstrapModule().deepLinkResolver()

    @Test
    fun everyFeatureLink_resolvesToItsBackStack() {
        val table: Map<String, List<NavKey>> = mapOf(
            "ledger://postings" to listOf(PostingList),
            "ledger://postings/new" to listOf(PostingList, PostingEdit(null)),
            "ledger://postings/42" to listOf(PostingList, PostingDetail("42")),
            "ledger://postings/42/edit" to listOf(PostingList, PostingDetail("42"), PostingEdit("42")),
            "ledger://settings" to listOf(SettingsHome),
        )
        for ((link, backStack) in table) {
            assertEquals<List<NavKey>?>(backStack, resolver.resolve(link), "back stack for $link")
        }
    }

    @Test
    fun everyBackStack_startsAtASectionRoot() {
        // Navigator.goToDeepLink refuses a stack whose first key no section owns.
        val sectionRoots = setOf<NavKey>(PostingList, SettingsHome)
        val links = listOf(
            "ledger://postings",
            "ledger://postings/new",
            "ledger://postings/42",
            "ledger://postings/42/edit",
            "ledger://settings",
        )
        for (link in links) {
            val root = resolver.resolve(link)?.first()
            assertTrue(root in sectionRoots, "$link starts at $root")
        }
    }

    @Test
    fun linkOutsideTheAppsScheme_isNotClaimed() {
        assertNull(resolver.resolve("https://postings/42"))
        assertNull(resolver.resolve("ledger://unknown"))
    }
}
