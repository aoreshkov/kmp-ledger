package app.oreshkov.kmpledger.core.domain

import app.oreshkov.kmpledger.core.data.repository.PostingRepository
import app.oreshkov.kmpledger.core.model.data.Posting
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetPostingsUseCase(
    private val repository: PostingRepository
) {
    operator fun invoke(): Flow<List<Posting>> = repository.getAllPostings()
}