package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.domain.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.Posting
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class GetPostingsUseCase(
    @Provided private val repository: PostingRepository
) {
    operator fun invoke(): Flow<List<Posting>> = repository.getAllPostings()
}