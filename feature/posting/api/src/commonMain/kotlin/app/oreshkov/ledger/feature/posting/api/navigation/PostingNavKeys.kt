package app.oreshkov.ledger.feature.posting.api.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import androidx.navigation3.runtime.NavKey

@Serializable data object PostingList : NavKey

@Serializable data class PostingDetail(val id: Long) : NavKey

@Serializable data class PostingEdit(val id: Long?) : NavKey

val serializerPostings = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(PostingList::class, PostingList.serializer())
        subclass(PostingDetail::class, PostingDetail.serializer())
        subclass(PostingEdit::class, PostingEdit.serializer())
    }
}