package app.oreshkov.kmpledger.core.data.model

import app.oreshkov.kmpledger.core.database.model.PostingEntity
import app.oreshkov.kmpledger.core.model.data.Posting
import app.oreshkov.kmpledger.core.model.data.NewPosting

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