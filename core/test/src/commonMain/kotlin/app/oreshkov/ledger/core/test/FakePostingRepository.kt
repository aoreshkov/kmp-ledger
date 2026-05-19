package app.oreshkov.ledger.core.test

import app.oreshkov.ledger.core.domain.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePostingRepository : PostingRepository {

    private val _postings = MutableStateFlow<List<Posting>>(emptyList())
    private var nextId = 1L

    val insertedPostings = mutableListOf<NewPosting>()
    val deletedPostings  = mutableListOf<Posting>()
    val updatedPostings  = mutableListOf<Posting>()

    fun seed(vararg postings: Posting) { _postings.value = postings.toList() }

    var failNextWrite: Boolean = false

    override suspend fun insertPosting(posting: NewPosting) {
        if (failNextWrite) { failNextWrite = false; error("DB error") }
        insertedPostings += posting
        _postings.update { it + Posting(nextId++, posting.narrative) }
    }

    override suspend fun deletePosting(posting: Posting) {
        deletedPostings += posting
        _postings.update { it.filterNot { c -> c.id == posting.id } }
    }

    override suspend fun updatePosting(posting: Posting) {
        if (failNextWrite) { failNextWrite = false; error("DB error") }
        updatedPostings += posting
        _postings.update { list -> list.map { if (it.id == posting.id) posting else it } }
    }

    var shouldThrowOnGetAll: Boolean = false

    override fun getAllPostings(): Flow<List<Posting>> = flow {
        if (shouldThrowOnGetAll) error("DB error")
        emitAll(_postings)
    }

    var shouldThrowOnGetById: Boolean = false

    override fun getPostingById(id: Long): Flow<Posting?> = flow {
        if (shouldThrowOnGetById) error("DB error")
        emitAll(_postings.map { list -> list.find { it.id == id } })
    }
}