package app.oreshkov.ledger.core.model.data

import kotlin.time.Instant

data class NewPosting(
    val amount: Long,
    val timestamp: Instant,
    val currency: String,
    val narrative: String,
)