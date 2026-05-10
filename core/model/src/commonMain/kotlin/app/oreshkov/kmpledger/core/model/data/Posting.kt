package app.oreshkov.kmpledger.core.model.data

import kotlin.time.Instant

data class Posting(
    val id: Long,
    val amount: Long,
    val timestamp: Instant,
    val currency: String,
    val narrative: String,
)