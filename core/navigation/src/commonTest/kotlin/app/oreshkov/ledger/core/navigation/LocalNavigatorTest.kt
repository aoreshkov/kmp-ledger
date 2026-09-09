package app.oreshkov.ledger.core.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LocalNavigatorTest : PlatformComposeUiTest() {

    @Test
    fun readingCurrent_withoutAProvider_failsWithADiagnosticMessage() {
        var failure: Throwable? = null
        try {
            runComposeUiTest { setContent { LocalNavigator.current } }
        } catch (e: Throwable) {
            failure = e
        }

        val thrown = assertNotNull(
            failure,
            "expected reading LocalNavigator.current without a provider to fail",
        )
        // Compose may wrap a composition failure, so match anywhere in the cause chain.
        val messages = generateSequence(thrown) { it.cause }.mapNotNull { it.message }.toList()
        assertTrue(
            messages.any { "No Navigator provided" in it },
            "expected 'No Navigator provided' in the cause chain, got: $messages",
        )
    }
}
