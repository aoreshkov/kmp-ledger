package app.oreshkov.ledger.feature.settings.api.navigation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import androidx.navigation3.runtime.NavKey

@Serializable
sealed interface SettingsRoute : NavKey

@Serializable
data object SettingsHome : SettingsRoute

@OptIn(ExperimentalSerializationApi::class)
val serializerSettings = SerializersModule {
    polymorphic(NavKey::class) {
        subclassesOfSealed<SettingsRoute>()
    }
}
