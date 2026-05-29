package app.oreshkov.ledger.core.common.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun randomUuidString(): String = Uuid.random().toString()