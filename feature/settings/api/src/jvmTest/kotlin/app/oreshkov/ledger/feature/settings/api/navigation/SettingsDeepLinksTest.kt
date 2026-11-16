package app.oreshkov.ledger.feature.settings.api.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * JVM only. On the Android host `DeepLinkUri` is `android.net.Uri`, a stub without Robolectric,
 * which this module does not carry; `core:bootstrap` runs the combined table on every target.
 */
class SettingsDeepLinksTest {

    private val matchers = settingsDeepLinks("ledger://")

    private fun resolve(uri: String): List<NavKey>? {
        val request = DeepLinkRequest(uri)
        return matchers.mapNotNull { it.match(request) }.maxOrNull()?.backStack
    }

    @Test
    fun settings_opensTheSectionRoot() {
        assertEquals<List<NavKey>?>(listOf(SettingsHome), resolve("ledger://settings"))
    }

    @Test
    fun otherLinks_areNotClaimed() {
        for (link in listOf("ledger://settings/theme", "ledger://postings", "https://settings")) {
            assertNull(resolve(link), "expected no match for \"$link\"")
        }
    }
}
