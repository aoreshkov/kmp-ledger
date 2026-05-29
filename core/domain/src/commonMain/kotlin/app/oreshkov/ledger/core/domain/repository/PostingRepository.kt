package app.oreshkov.ledger.core.domain.repository

import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting
import kotlinx.coroutines.flow.Flow

interface PostingRepository {
    suspend fun insertPosting(posting: NewPosting)
    suspend fun deletePosting(posting: Posting)
    suspend fun updatePosting(posting: Posting)
    fun getPostingById(id: String): Flow<Posting?>
    fun getAllPostings(): Flow<List<Posting>>
}