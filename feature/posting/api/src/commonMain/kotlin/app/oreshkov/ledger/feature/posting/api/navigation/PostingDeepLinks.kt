package app.oreshkov.ledger.feature.posting.api.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatchResult
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.withBackStack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder

/**
 * The links into this feature, each with the back stack it opens on. Every stack starts at
 * [PostingList], the section root, so Back from a linked screen walks up to the list.
 *
 * | Link                         | Back stack                                 |
 * |------------------------------|--------------------------------------------|
 * | `<base>postings`             | `PostingList`                              |
 * | `<base>postings/new`         | `PostingList`, `PostingEdit(null)`         |
 * | `<base>postings/{id}`        | `PostingList`, `PostingDetail`             |
 * | `<base>postings/{id}/edit`   | `PostingList`, `PostingDetail`, `PostingEdit` |
 *
 * `new` and `{id}` both match `postings/new`; the exact path outranks the placeholder, so the
 * add screen wins.
 *
 * @param baseUri everything before the path, ending in its separator: `ledger://` for the
 *   app's own scheme, `https://example.com/` for web links.
 */
fun postingDeepLinks(
    baseUri: String,
): List<DeepLinkMatcher<NavKey, BackStackMatchResult<NavKey, NavKey>>> = listOf(
    UriDeepLinkMatcher(DeepLinkUri("${baseUri}postings"), PostingList.serializer())
        .withBackStack<PostingList, NavKey> { match -> listOf(match.key) },
    UriDeepLinkMatcher(DeepLinkUri("${baseUri}postings/new"), NewPostingSerializer)
        .withBackStack<PostingEdit, NavKey> { match -> postingEditStack(match.key) },
    UriDeepLinkMatcher(DeepLinkUri("${baseUri}postings/{id}"), PostingDetail.serializer())
        .withBackStack<PostingDetail, NavKey> { match -> listOf(PostingList, match.key) },
    UriDeepLinkMatcher(DeepLinkUri("${baseUri}postings/{id}/edit"), PostingEdit.serializer())
        .withBackStack<PostingEdit, NavKey> { match -> postingEditStack(match.key) },
)

/** Editing an existing posting sits above its details; adding one sits directly above the list. */
private fun postingEditStack(edit: PostingEdit): List<NavKey> =
    listOfNotNull(PostingList, edit.id?.let(::PostingDetail), edit)

/**
 * Builds the key for `postings/new`, which carries no argument to decode. [PostingEdit.id] has
 * no default on purpose: a saved back-stack entry that lost its id must fail to restore rather
 * than quietly become an add screen. So the "no id" case is spelled out here, for this one link.
 */
private object NewPostingSerializer : KSerializer<PostingEdit> by PostingEdit.serializer() {
    override fun deserialize(decoder: Decoder): PostingEdit = PostingEdit(id = null)
}
