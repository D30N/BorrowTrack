package com.deon.borrowtrack

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
    val returnedMs: Long
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
