package app.oreshkov.ledger.core.data.model

import app.oreshkov.ledger.core.database.model.PostingEntity
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting

fun PostingEntity.asExternalModel() = Posting(
    id = id,
    amount = amount,
    timestamp = timestamp,
    currency = currency,
    narrative = narrative,
)
fun NewPosting.asEntity() = PostingEntity(
    id = 0L,
    amount = amount,
    timestamp = timestamp,
    currency = currency,
    narrative = narrative,
)

fun Posting.asEntity() = PostingEntity(
    id = id,
    amount = amount,
    timestamp = timestamp,
    currency = currency,
    narrative = narrative,
)