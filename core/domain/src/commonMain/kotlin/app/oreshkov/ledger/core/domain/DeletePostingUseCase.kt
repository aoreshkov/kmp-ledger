package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.domain.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.Posting
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class DeletePostingUseCase(
    @Provided private val repository: PostingRepository
) {
    suspend operator fun invoke(posting: Posting) = repository.deletePosting(posting)
}