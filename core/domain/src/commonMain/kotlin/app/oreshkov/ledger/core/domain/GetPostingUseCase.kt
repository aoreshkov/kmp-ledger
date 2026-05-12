package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.data.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.Posting
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetPostingUseCase(
    private val repository: PostingRepository
) {
    operator fun invoke(id: Long): Flow<Posting?> = repository.getPostingById(id)
}