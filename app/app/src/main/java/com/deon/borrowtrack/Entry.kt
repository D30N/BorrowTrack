package com.deon.borrowtrack

/** One partial repayment: amount + when it happened. */
data class Payment(val amount: Double, val ms: Long)

/** type: "lent" (I gave) or "borrowed" (I took) */
data class Entry(
    val id: String,
    val type: String,
    val person: String,
    val item: String,
    val dateMs: Long,
    val dueMs: Long,
    val note: String,
    val returned: Boolean,
    val returnedMs: Long,
    val payments: List<Payment> = emptyList()
) {
    val isLent: Boolean get() = type == "lent"

    /** days overdue (>0) or days left (<=0 means due today/overdue boundary) */
    fun daysOverdue(now: Long = System.currentTimeMillis()): Long {
        val diff = now - dueMs
        return if (diff <= 0) 0 else diff / 86_400_000
    }

    fun daysLeft(now: Long = System.currentTimeMillis()): Long {
        val diff = dueMs - now
        return if (diff < 0) 0 else diff / 86_400_000
    }
}
