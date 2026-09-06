package app.oreshkov.ledger.core.common.util

import kotlin.uuid.Uuid

fun randomUuidString(): String = Uuid.random().toString()