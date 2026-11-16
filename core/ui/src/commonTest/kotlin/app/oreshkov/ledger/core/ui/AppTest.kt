package app.oreshkov.ledger.core.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.withBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.domain.GetThemeModeUseCase
import app.oreshkov.ledger.core.navigation.DeepLinkResolver
import app.oreshkov.ledger.core.navigation.StartDestination
import app.oreshkov.ledger.core.test.FakeSettingsRepository
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.compose.KoinIsolatedContext
import org.koin.plugin.module.dsl.koinApplication
import org.koin.plugin.module.dsl.koinConfiguration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.koin.compose.KoinApplication as KoinApplicationScope

@Serializable
data class TestKey(val id: String = "test") : NavKey

/**
 * Annotated so the Koin compiler plugin can resolve this test's graph statically. Passing a
 * `module { }` lambda straight to `koinConfiguration` makes the module set dynamically computed,
 * which downgrades the entry point to runtime-only checking (KOIN-W003).
 */
@Module
class AppTestModule {
    @Single
    fun startDestination() = StartDestination(TestKey())

    @Single
    fun getThemeMode() = GetThemeModeUseCase(FakeSettingsRepository())

    @Single
    fun savedStateConfiguration() = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) { subclass(TestKey::class) }
        }
    }

    // `test://items/{id}` opens TestKey(id) above the start route, TestKey("test").
    @Single
    fun deepLinkResolver() = DeepLinkResolver(
        listOf(
            UriDeepLinkMatcher(DeepLinkUri("test://items/{id}"), TestKey.serializer())
                .withBackStack<TestKey, NavKey> { match -> listOf(TestKey(), match.key) },
        ),
    )
}

@KoinApplication(modules = [AppTestModule::class])
class TestApp

// `navigation<T>` has no annotation equivalent, so the screen entry stays DSL — the same split
// the feature `*NavigationModule`s use.
@OptIn(KoinExperimentalAPI::class)
private val testNavigationModule = module {
    navigation<TestKey> {
        Text("Hello from TestKey")
    }
}

@OptIn(ExperimentalTestApi::class, KoinExperimentalAPI::class, ExperimentalCoroutinesApi::class)
class AppTest : PlatformComposeUiTest() {

    // App() collects the theme flow via collectAsStateWithLifecycle, which dispatches on
    // Dispatchers.Main; coroutines-test leaves Main uninitialized until setMain is called.
    @BeforeTest fun setUp()    { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun app_rendersStartDestination() = runComposeUiTest {
        setContent {
            KoinApplicationScope(
                configuration = koinConfiguration<TestApp> {
                    modules(testNavigationModule)
                }
            ) {
                App()
            }
        }

        onNodeWithText("Hello from TestKey").assertIsDisplayed()
    }

    private val screenPerKey = module {
        navigation<TestKey> { key -> Text("screen=${key.id}") }
    }

    // An isolated context, as in AppStateRestorationTest: the KoinApplication composable above
    // leaves the global Koin running, and these tests must not inherit its "Hello" entry.
    private val koin = koinApplication<TestApp> { modules(screenPerKey) }

    @AfterTest fun closeKoin() { koin.close() }

    @Test
    fun deepLink_opensItsBackStack_andIsAcknowledged() = runComposeUiTest {
        var pending by mutableStateOf<String?>("test://items/42")
        var acknowledged = 0
        setContent {
            KoinIsolatedContext(context = koin) {
                App(
                    deepLink = pending,
                    onDeepLinkConsumed = {
                        acknowledged++
                        pending = null
                    },
                )
            }
        }

        onNodeWithText("screen=42").assertIsDisplayed()
        assertEquals(1, acknowledged)
        assertNull(pending)
    }

    @Test
    fun deepLink_arrivingWhileRunning_replacesWhatTheSectionShowed() = runComposeUiTest {
        var pending by mutableStateOf<String?>(null)
        setContent {
            KoinIsolatedContext(context = koin) {
                App(deepLink = pending, onDeepLinkConsumed = { pending = null })
            }
        }
        onNodeWithText("screen=test").assertIsDisplayed()

        pending = "test://items/1"
        onNodeWithText("screen=1").assertIsDisplayed()

        // The acknowledgement cleared the state, so a later link (this one or the same one
        // again) is a change the effect sees.
        pending = "test://items/2"
        onNodeWithText("screen=2").assertIsDisplayed()
        onNodeWithText("screen=1").assertDoesNotExist()
    }

    @Test
    fun unclaimedDeepLink_isAcknowledged_andLeavesTheScreenAlone() = runComposeUiTest {
        var pending by mutableStateOf<String?>("test://nowhere")
        var acknowledged = 0
        setContent {
            KoinIsolatedContext(context = koin) {
                App(
                    deepLink = pending,
                    onDeepLinkConsumed = {
                        acknowledged++
                        pending = null
                    },
                )
            }
        }

        onNodeWithText("screen=test").assertIsDisplayed()
        assertEquals(1, acknowledged)
        assertNull(pending)
    }
}
