package app.oreshkov.kmpledger.core.domain

import app.oreshkov.kmpledger.core.data.repository.PostingRepository
import app.oreshkov.kmpledger.core.model.data.Posting
import org.koin.core.annotation.Factory

@Factory
class DeletePostingUseCase(
    private val repository: PostingRepository
) {
    suspend operator fun invoke(posting: Posting) = repository.deletePosting(posting)
}