package app.oreshkov.kmpledger.core.domain

import app.oreshkov.kmpledger.core.data.repository.PostingRepository
import app.oreshkov.kmpledger.core.model.data.Posting
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetPostingUseCase(
    private val repository: PostingRepository
) {
    operator fun invoke(id: Long): Flow<Posting?> = repository.getPostingById(id)
}