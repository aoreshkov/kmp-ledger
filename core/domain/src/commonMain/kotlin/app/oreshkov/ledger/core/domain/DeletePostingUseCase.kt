package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.domain.repository.PostingRepository
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class DeletePostingUseCase(
    @Provided private val repository: PostingRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching {
        repository.deletePosting(id)
    }
}