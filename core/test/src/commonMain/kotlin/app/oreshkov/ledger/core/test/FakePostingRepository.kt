package app.oreshkov.ledger.core.test

import app.oreshkov.ledger.core.common.util.randomUuidString
import app.oreshkov.ledger.core.domain.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePostingRepository : PostingRepository {

    private val _postings = MutableStateFlow<List<Posting>>(emptyList())

    val insertedPostings = mutableListOf<NewPosting>()
    val deletedPostings  = mutableListOf<Posting>()
    val updatedPostings  = mutableListOf<Posting>()

    fun seed(vararg postings: Posting) { _postings.value = postings.toList() }

    var failNextWrite: Boolean = false

    /**
     * Opt-in suspension point: while set, every write suspends on it before running, so a
     * test can hold a write in flight (e.g. to tap Save again) and release it with
     * [CompletableDeferred.complete].
     */
    var writeGate: CompletableDeferred<Unit>? = null

    private suspend fun beforeWrite() {
        writeGate?.await()
        if (failNextWrite) { failNextWrite = false; error("DB error") }
    }

    override suspend fun insertPosting(posting: NewPosting) {
        beforeWrite()
        insertedPostings += posting
        _postings.update { it + Posting(randomUuidString(), posting.narrative) }
    }

    override suspend fun deletePosting(id: String) {
        beforeWrite()
        val posting = _postings.value.find { it.id == id }
        if (posting != null) deletedPostings += posting
        _postings.update { it.filterNot { c -> c.id == id } }
    }

    override suspend fun updatePosting(posting: Posting) {
        beforeWrite()
        updatedPostings += posting
        _postings.update { list -> list.map { if (it.id == posting.id) posting else it } }
    }

    var shouldThrowOnGetAll: Boolean = false

    override fun getAllPostings(): Flow<List<Posting>> = flow {
        if (shouldThrowOnGetAll) error("DB error")
        emitAll(_postings)
    }

    var shouldThrowOnGetById: Boolean = false

    override fun getPostingById(id: String): Flow<Posting?> = flow {
        if (shouldThrowOnGetById) error("DB error")
        emitAll(_postings.map { list -> list.find { it.id == id } })
    }
}
