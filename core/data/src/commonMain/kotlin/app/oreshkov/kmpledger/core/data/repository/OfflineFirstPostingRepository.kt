package app.oreshkov.kmpledger.core.data.repository

import app.oreshkov.kmpledger.core.data.model.asEntity
import app.oreshkov.kmpledger.core.data.model.asExternalModel
import app.oreshkov.kmpledger.core.database.dao.PostingDao
import app.oreshkov.kmpledger.core.model.data.Posting
import app.oreshkov.kmpledger.core.model.data.NewPosting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstPostingRepository(private val postingDao: PostingDao) : PostingRepository {
    override suspend fun insertPosting(posting: NewPosting) =
        postingDao.insert(posting = posting.asEntity())

    override suspend fun deletePosting(posting: Posting) =
        postingDao.delete(posting = posting.asEntity())

    override suspend fun updatePosting(posting: Posting) =
        postingDao.update(posting = posting.asEntity())

    override fun getPostingById(id: Long): Flow<Posting?> =
        postingDao.getPostingById(id).map { it?.asExternalModel() }

    override fun getAllPostings(): Flow<List<Posting>> =
        postingDao.getAllPostings().map { postings ->
            postings.map { it.asExternalModel() }
        }
}