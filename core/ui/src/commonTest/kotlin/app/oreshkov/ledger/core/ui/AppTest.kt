package app.oreshkov.ledger.core.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import app.oreshkov.ledger.core.navigation.StartDestination
import app.oreshkov.ledger.core.test.PlatformComposeUiTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.test.AfterTest
import kotlin.test.Test

@Serializable
data class TestKey(val id: String = "test") : NavKey

@OptIn(ExperimentalTestApi::class, KoinExperimentalAPI::class)
class AppTest : PlatformComposeUiTest() {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun app_rendersStartDestination() = runComposeUiTest {
        val startKey = TestKey()
        startKoin {
            modules(
                module {
                    single { StartDestination(startKey) }
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

        setContent {
            App()
        }

        onNodeWithText("Hello from TestKey").assertIsDisplayed()
    }
}