package app.oreshkov.ledger.core.data.model

import app.oreshkov.ledger.core.database.model.PostingEntity
import app.oreshkov.ledger.core.model.data.Posting
import app.oreshkov.ledger.core.model.data.NewPosting

import app.oreshkov.ledger.core.common.util.randomUuidString

fun PostingEntity.asExternalModel() = Posting(
    id = id,
    narrative = narrative,
)

fun NewPosting.asEntity() = PostingEntity(
    id = randomUuidString(),
    narrative = narrative,
)

fun Posting.asEntity() = PostingEntity(
    id = id,
    narrative = narrative,
)