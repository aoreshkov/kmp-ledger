package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.data.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting
import kotlin.time.Instant
import org.koin.core.annotation.Factory

@Factory
class SavePostingUseCase(
    private val repository: PostingRepository
) {
    suspend operator fun invoke(
        id: Long?,
        amount: Long,
        timestamp: Instant,
        currency: String,
        narrative: String
    ) {
        if (id == null) {
            repository.insertPosting(
                NewPosting(
                    amount = amount,
                    timestamp = timestamp,
                    currency = currency,
                    narrative = narrative
                )
            )
        } else {
            repository.updatePosting(
                Posting(
                    id = id,
                    amount = amount,
                    timestamp = timestamp,
                    currency = currency,
                    narrative = narrative
                )
            )
        }
    }
}