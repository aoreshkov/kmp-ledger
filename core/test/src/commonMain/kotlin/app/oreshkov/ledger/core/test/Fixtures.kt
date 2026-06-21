package app.oreshkov.ledger.core.test

import app.oreshkov.ledger.core.model.data.NewPosting
import app.oreshkov.ledger.core.model.data.Posting

/** Object Mother for posting test data. Defaults match the suite's long-standing "Groceries" sample. */

fun posting(
    id: String = "1",
    narrative: String = "Groceries",
): Posting = Posting(id = id, narrative = narrative)

fun newPosting(
    narrative: String = "Groceries",
): NewPosting = NewPosting(narrative = narrative)

/** Convenience for the common multi-row list case (PostingList screen/VM). */
fun postings(vararg narratives: String): List<Posting> =
    narratives.mapIndexed { index, narrative -> posting(id = (index + 1).toString(), narrative = narrative) }

/** N sequential postings: id "1".."N", narrative "Posting 1".."Posting N". */
fun postings(count: Int): List<Posting> =
    (1..count).map { posting(id = it.toString(), narrative = "Posting $it") }
