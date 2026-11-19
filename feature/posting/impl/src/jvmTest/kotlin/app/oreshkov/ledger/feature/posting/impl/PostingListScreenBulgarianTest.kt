package app.oreshkov.ledger.feature.posting.impl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Renders the list under the Bulgarian locale: proof that this module's `values-bg` resources
 * are packaged and picked. Key-for-key completeness is the `checkStringResourceParity` task's
 * job, not this test's.
 *
 * JVM only, because the default locale is switched through `java.util.Locale`.
 */
@OptIn(ExperimentalTestApi::class)
class PostingListScreenBulgarianTest {

    private val originalLocale: Locale = Locale.getDefault()

    @BeforeTest fun useBulgarian() { Locale.setDefault(Locale.forLanguageTag("bg")) }
    @AfterTest fun restoreLocale() { Locale.setDefault(originalLocale) }

    @Test
    fun showsBulgarianStrings() = runComposeUiTest {
        setContent {
            PostingListContent(
                uiState = PostingListUiState.Empty,
                onAddClick = {},
                onPostingClick = {},
                onRetry = {},
            )
        }

        onNodeWithText("Моите записи").assertIsDisplayed()
        onNodeWithText("Все още няма добавени записи.").assertIsDisplayed()
        onNodeWithContentDescription("Добавяне на запис").assertIsDisplayed()
    }
}
