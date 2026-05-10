package app.oreshkov.kmpledger.core.data.repository

import app.oreshkov.kmpledger.core.model.data.Posting
import app.oreshkov.kmpledger.core.model.data.NewPosting
import kotlinx.coroutines.flow.Flow

interface PostingRepository {
    suspend fun insertPosting(posting: NewPosting)
    suspend fun deletePosting(posting: Posting)
    suspend fun updatePosting(posting: Posting)
    fun getPostingById(id: Long): Flow<Posting?>
    fun getAllPostings(): Flow<List<Posting>>
}