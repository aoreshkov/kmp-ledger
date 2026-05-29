package app.oreshkov.ledger.core.domain

import app.oreshkov.ledger.core.domain.repository.PostingRepository
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
class SavePostingUseCase(
    @Provided private val repository: PostingRepository
) {
    suspend operator fun invoke(
        id: String?,
        narrative: String
    ) {
        require(narrative.isNotBlank()) { "Narrative cannot be blank" }

        if (id == null) {
            repository.insertPosting(
                NewPosting(
                    narrative = narrative
                )
            )
        } else {
            repository.updatePosting(
                Posting(
                    id = id,
                    narrative = narrative
                )
            )
        }
    }
}