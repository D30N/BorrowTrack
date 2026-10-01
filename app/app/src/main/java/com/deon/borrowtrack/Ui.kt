package com.deon.borrowtrack

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView

object Ui {
    fun dp(c: Context, v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), c.resources.displayMetrics
        ).toInt()

    fun color(c: Context, id: Int): Int =
        androidx.core.content.ContextCompat.getColor(c, id)

    fun tv(c: Context, text: String, sizeSp: Float, colorId: Int, bold: Boolean = false): TextView {
        val t = TextView(c)
        t.text = text
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        t.setTextColor(color(c, colorId))
        if (bold) t.typeface = Typeface.DEFAULT_BOLD
        return t
    }

    fun card(c: Context): LinearLayout {
        val l = LinearLayout(c)
        l.orientation = LinearLayout.VERTICAL
        l.setBackgroundResource(R.drawable.card)
        val p = dp(c, 12)
        l.setPadding(p, dp(c, 10), p, dp(c, 10))
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val m = dp(c, 5)
        lp.setMargins(dp(c, 10), m, dp(c, 10), m)
        l.layoutParams = lp
        l.isClickable = true
        l.isFocusable = true
        return l
    }

    fun badge(c: Context, e: Entry): TextView {
        val (text, colorId) = statusOf(e)
        val t = tv(c, text, 10f, colorId, true)
        t.setPadding(dp(c, 9), dp(c, 4), dp(c, 9), dp(c, 4))
        return t
    }

    /** Returns (label, colorRes). */
    fun statusOf(e: Entry): Pair<String, Int> {
        if (e.returned) return "DONE" to R.color.green
        val od = e.daysOverdue()
        return when {
            od > 0 -> "OVERDUE" to R.color.red
            e.daysLeft() <= 2 -> "DUE SOON" to R.color.amber
            else -> "OK" to R.color.green
        }
    }

    fun subLine(e: Entry): String {
        return if (e.isLent) {
            val od = e.daysOverdue()
            when {
                e.returned -> "${e.person} · തിരികെ കിട്ടി"
                od > 0 -> "${e.person} · $od ദിവസം overdue"
                else -> "${e.person} · ${Store.fmtDate(e.dueMs)} ന് തിരികെ"
            }
        } else {
            val od = e.daysOverdue()
            when {
                e.returned -> "${e.person}-ന് തിരികെ കൊടുത്തു"
                od > 0 -> "${e.person}-ന് $od ദിവസം മുൻപ് കൊടുക്കേണ്ടതായിരുന്നു"
                else -> "${e.person}-ന് ${Store.fmtDate(e.dueMs)} ന് കൊടുക്കണം"
            }
        }
    }

    fun tabBtn(
        c: Context, icon: String, label: String, on: Boolean, onClick: () -> Unit
    ): LinearLayout {
        val l = LinearLayout(c)
        l.orientation = LinearLayout.VERTICAL
        l.gravity = Gravity.CENTER
        l.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        l.setPadding(0, dp(c, 7), 0, dp(c, 10))
        l.isClickable = true
        l.isFocusable = true
        val ic = tv(c, icon, 17f, if (on) R.color.amber else R.color.grey2)
        ic.gravity = Gravity.CENTER
        val lb = tv(c, label, 10f, if (on) R.color.amber else R.color.grey2, on)
        lb.gravity = Gravity.CENTER
        lb.setPadding(0, dp(c, 2), 0, 0)
        l.addView(ic)
        l.addView(lb)
        l.setOnClickListener { onClick() }
        return l
    }

    fun spacer(c: Context): Space =
        Space(c).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) }
}
