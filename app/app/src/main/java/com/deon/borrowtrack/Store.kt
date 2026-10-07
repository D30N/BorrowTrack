package com.deon.borrowtrack

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object Store {
    private const val NAME = "bt"

    private fun sp(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun all(ctx: Context): List<Entry> {
        val raw = sp(ctx).getString("entries", "[]") ?: "[]"
        val out = ArrayList<Entry>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    Entry(
                        id = o.optString("id"),
                        type = o.optString("type", "lent"),
                        person = o.optString("person"),
                        item = o.optString("item"),
                        dateMs = o.optLong("dateMs"),
                        dueMs = o.optLong("dueMs"),
                        note = o.optString("note"),
                        returned = o.optBoolean("returned"),
                        returnedMs = o.optLong("returnedMs")
                    )
                )
            }
        } catch (_: Exception) {
        }
        out.sortWith(compareBy({ it.returned }, { -it.dueMs }))
        return out
    }

    fun byId(ctx: Context, id: String): Entry? = all(ctx).find { it.id == id }

    private fun saveAll(ctx: Context, list: List<Entry>) {
        val arr = JSONArray()
        for (e in list) {
            arr.put(
                JSONObject()
                    .put("id", e.id).put("type", e.type)
                    .put("person", e.person).put("item", e.item)
                    .put("dateMs", e.dateMs).put("dueMs", e.dueMs)
                    .put("note", e.note).put("returned", e.returned)
                    .put("returnedMs", e.returnedMs)
            )
        }
        sp(ctx).edit().putString("entries", arr.toString()).apply()
        LentWidgetProvider.updateAll(ctx)
        BorrowedWidgetProvider.updateAll(ctx)
    }

    fun add(
        ctx: Context, type: String, person: String, item: String,
        dateMs: Long, dueMs: Long, note: String
    ): Entry {
        val e = Entry(
            UUID.randomUUID().toString(), type, person.trim(), item.trim(),
            dateMs, dueMs, note.trim(), false, 0
        )
        val list = all(ctx).toMutableList()
        list.add(e)
        saveAll(ctx, list)
        Remind.schedule(ctx, e)
        return e
    }

    fun markReturned(ctx: Context, id: String) {
        val list = all(ctx).toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i >= 0) {
            list[i] = list[i].copy(returned = true, returnedMs = System.currentTimeMillis())
            saveAll(ctx, list)
            Remind.cancel(ctx, id)
        }
    }

    fun updatePartial(ctx: Context, id: String, returnedAmount: Double) {
        val list = all(ctx).toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i < 0) return
        val e = list[i]

        val numberRegex = Regex("""\d+(\.\d+)?""")
        val match = numberRegex.find(e.item)
        if (match != null) {
            val currentAmount = match.value.toDoubleOrNull() ?: 0.0
            val remaining = currentAmount - returnedAmount
            if (remaining <= 0) {
                markReturned(ctx, id)
            } else {
                val remainingStr = if (remaining % 1.0 == 0.0) remaining.toLong().toString() else remaining.toString()
                val newItem = e.item.replaceFirst(match.value, remainingStr)
                list[i] = e.copy(item = newItem)
                saveAll(ctx, list)
            }
        } else {
            markReturned(ctx, id)
        }
    }

    fun delete(ctx: Context, id: String) {
        saveAll(ctx, all(ctx).filter { it.id != id })
        Remind.cancel(ctx, id)
    }

    fun fmtDate(ms: Long): String {
        if (ms <= 0) return "—"
        val c = java.util.Calendar.getInstance()
        c.timeInMillis = ms
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${c.get(java.util.Calendar.DAY_OF_MONTH)} ${months[c.get(java.util.Calendar.MONTH)]} ${c.get(java.util.Calendar.YEAR)}"
    }
}
