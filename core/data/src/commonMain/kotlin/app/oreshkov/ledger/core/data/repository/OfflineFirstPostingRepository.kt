package app.oreshkov.ledger.core.data.repository

import app.oreshkov.ledger.core.common.dispatcher.AppDispatchers
import app.oreshkov.ledger.core.data.model.asEntity
import app.oreshkov.ledger.core.data.model.asExternalModel
import app.oreshkov.ledger.core.database.dao.PostingDao
import app.oreshkov.ledger.core.domain.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OfflineFirstPostingRepository(
    private val postingDao: PostingDao,
    private val dispatchers: AppDispatchers,
) : PostingRepository {
    override suspend fun insertPosting(posting: NewPosting) =
        withContext(dispatchers.io) { postingDao.insert(posting = posting.asEntity()) }

    override suspend fun deletePosting(id: String) =
        withContext(dispatchers.io) { postingDao.deleteById(id) }

    override suspend fun updatePosting(posting: Posting) =
        withContext(dispatchers.io) { postingDao.update(posting = posting.asEntity()) }

    override fun getPostingById(id: String): Flow<Posting?> =
        postingDao.getPostingById(id).map { it?.asExternalModel() }.flowOn(dispatchers.io)

    override fun getAllPostings(): Flow<List<Posting>> =
        postingDao.getAllPostings().map { postings ->
            postings.map { it.asExternalModel() }
        }.flowOn(dispatchers.io)
}