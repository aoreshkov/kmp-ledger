package app.oreshkov.ledger.feature.posting.api.navigation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import androidx.navigation3.runtime.NavKey

@Serializable
sealed interface PostingRoute : NavKey

@Serializable
data object PostingList : PostingRoute

@Serializable
data class PostingDetail(val id: Long) : PostingRoute

@Serializable
data class PostingEdit(val id: Long?) : PostingRoute

@OptIn(ExperimentalSerializationApi::class)
val serializerPostings = SerializersModule {
    polymorphic(NavKey::class) {
        subclassesOfSealed<PostingRoute>()
    }
}