package app.oreshkov.ledger.core.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.domain.GetThemeModeUseCase
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
import org.koin.compose.KoinApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@Serializable
data class TestKey(val id: String = "test") : NavKey

@OptIn(ExperimentalTestApi::class, KoinExperimentalAPI::class, ExperimentalCoroutinesApi::class)
class AppTest : PlatformComposeUiTest() {

    // App() collects the theme flow via collectAsStateWithLifecycle, which dispatches on
    // Dispatchers.Main; coroutines-test leaves Main uninitialized until setMain is called.
    @BeforeTest fun setUp()    { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun app_rendersStartDestination() = runComposeUiTest {
        val startKey = TestKey()

        setContent {
            KoinApplication(
                configuration = koinConfiguration {
                    modules(
                        module {
                            single { StartDestination(startKey) }
                            single { GetThemeModeUseCase(FakeSettingsRepository()) }
                            single {
                                SavedStateConfiguration {
                                    serializersModule = SerializersModule {
                                        polymorphic(NavKey::class) {
                                            subclass(TestKey::class)
                                        }
                                    }
                                }
                            }

                            navigation<TestKey> {
                                Text("Hello from TestKey")
                            }
                        }
                    )
                }
            ) {
                App()
            }
        }

        onNodeWithText("Hello from TestKey").assertIsDisplayed()
    }
}
