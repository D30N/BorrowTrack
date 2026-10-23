package com.deon.borrowtrack

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView

object Ui {
    fun dp(c: Context, v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), c.resources.displayMetrics
        ).toInt()

    fun color(c: Context, id: Int): Int = Theme.colorFor(c, id)

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
        l.background = Theme.cardBg(c)
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

    fun subLine(c: Context, e: Entry): String {
        val en = Theme.lang(c) == "en"
        return if (e.isLent) {
            val od = e.daysOverdue()
            when {
                e.returned -> if (en) "${e.person} · returned" else "${e.person} · തിരികെ കിട്ടി"
                od > 0 -> if (en) "${e.person} · $od days overdue" else "${e.person} · $od ദിവസം overdue"
                else -> if (en) "${e.person} · back by ${Store.fmtDate(e.dueMs)}"
                    else "${e.person} · ${Store.fmtDate(e.dueMs)} ന് തിരികെ"
            }
        } else {
            val od = e.daysOverdue()
            when {
                e.returned -> if (en) "Returned to ${e.person}" else "${e.person}-ന് തിരികെ കൊടുത്തു"
                od > 0 -> if (en) "Was due to ${e.person} $od days ago"
                    else "${e.person}-ന് $od ദിവസം മുൻപ് കൊടുക്കേണ്ടതായിരുന്നു"
                else -> if (en) "Return to ${e.person} by ${Store.fmtDate(e.dueMs)}"
                    else "${e.person}-ന് ${Store.fmtDate(e.dueMs)} ന് കൊടുക്കണം"
            }
        }
    }

    fun tabBtn(
        c: Context, iconRes: Int, label: String, on: Boolean, onClick: () -> Unit
    ): LinearLayout {
        val l = LinearLayout(c)
        l.orientation = LinearLayout.VERTICAL
        l.gravity = Gravity.CENTER
        l.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        l.setPadding(0, dp(c, 7), 0, dp(c, 10))
        l.isClickable = true
        l.isFocusable = true
        val ic = ImageView(c)
        ic.setImageResource(iconRes)
        ic.setColorFilter(color(c, if (on) R.color.amber else R.color.grey2))
        val isz = dp(c, 26)
        ic.layoutParams = LinearLayout.LayoutParams(isz, isz)
        val lb = tv(c, label, 12f, if (on) R.color.amber else R.color.grey2, on)
        lb.gravity = Gravity.CENTER
        lb.setPadding(0, dp(c, 3), 0, 0)
        l.addView(ic)
        l.addView(lb)
        l.setOnClickListener { onClick() }
        return l
    }

    /** A button whose + is drawn geometrically — always dead centre. */
    fun plusButton(
        c: Context, sizeDp: Int, bg: Drawable?, fg: Int, onClick: () -> Unit
    ): FrameLayout {
        val f = FrameLayout(c)
        f.background = bg
        val s = dp(c, sizeDp)
        f.layoutParams = LinearLayout.LayoutParams(s, s)
        val icon = View(c)
        val isz = dp(c, (sizeDp * 0.42f).toInt().coerceAtLeast(14))
        icon.layoutParams = FrameLayout.LayoutParams(isz, isz, Gravity.CENTER)
        icon.background = PlusDrawable(fg)
        f.addView(icon)
        f.isClickable = true
        f.isFocusable = true
        f.setOnClickListener { onClick() }
        return f
    }

    fun spacer(c: Context): Space =
        Space(c).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) }
}

/** Draws a + with two centred strokes — immune to font-metric drift. */
class PlusDrawable(private val fg: Int) : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        val m = Math.min(b.width(), b.height()).toFloat()
        p.color = fg
        p.strokeWidth = m * 0.18f
        val arm = m * 0.32f
        val cx = b.exactCenterX()
        val cy = b.exactCenterY()
        canvas.drawLine(cx - arm, cy, cx + arm, cy, p)
        canvas.drawLine(cx, cy - arm, cx, cy + arm, p)
    }

    override fun setAlpha(alpha: Int) {
        p.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        p.colorFilter = cf
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/** A radio circle drawn at full size — ring plus centre dot when checked. */
class RadioDrawable(private val checked: Boolean, private val fg: Int) : Drawable() {
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    override fun draw(canvas: Canvas) {
        val b = bounds
        val m = Math.min(b.width(), b.height()).toFloat()
        ring.color = fg
        ring.strokeWidth = m * 0.09f
        dot.color = fg
        val cx = b.exactCenterX()
        val cy = b.exactCenterY()
        canvas.drawCircle(cx, cy, m * 0.40f, ring)
        if (checked) canvas.drawCircle(cx, cy, m * 0.21f, dot)
    }

    override fun setAlpha(alpha: Int) {
        ring.alpha = alpha
        dot.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        ring.colorFilter = cf
        dot.colorFilter = cf
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
