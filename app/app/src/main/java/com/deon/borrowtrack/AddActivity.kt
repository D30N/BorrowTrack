package com.deon.borrowtrack

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.util.Calendar

class AddActivity : Activity() {

    private var isLent = true
    private var dateMs: Long = System.currentTimeMillis()
    private var dueMs: Long = System.currentTimeMillis() + 7 * 86_400_000L

    private lateinit var personEt: EditText
    private lateinit var itemEt: EditText
    private lateinit var noteEt: EditText
    private lateinit var dateTv: TextView
    private lateinit var dueTv: TextView
    private lateinit var togA: TextView
    private lateinit var togB: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.color(this@AddActivity, R.color.bg))
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val p = Ui.dp(this@AddActivity, 14)
            setPadding(p, Ui.dp(this@AddActivity, 10), p, Ui.dp(this@AddActivity, 8))
        }
        val back = Ui.tv(this, "‹", 26f, R.color.white).apply {
            setPadding(0, 0, Ui.dp(this@AddActivity, 12), 0)
            isClickable = true; isFocusable = true
            setOnClickListener { finish() }
        }
        bar.addView(back)
        bar.addView(Ui.tv(this, "പുതിയ entry", 16f, R.color.white, true))
        root.addView(bar)

        val sv = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val f = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = Ui.dp(this@AddActivity, 16)
            setPadding(p, Ui.dp(this@AddActivity, 4), p, p)
        }

        // Toggle
        val tog = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.card)
            setPadding(Ui.dp(this@AddActivity, 3), Ui.dp(this@AddActivity, 3),
                Ui.dp(this@AddActivity, 3), Ui.dp(this@AddActivity, 3))
        }
        togA = togBtn("ഞാൻ കൊടുത്തു", true) { isLent = true; paintTog() }
        togB = togBtn("ഞാൻ വാങ്ങി", false) { isLent = false; paintTog() }
        tog.addView(togA); tog.addView(togB)
        f.addView(tog)
        paintTog()

        f.addView(flabel("ആര്"))
        personEt = field("ഉദാ: Arun")
        f.addView(personEt)

        f.addView(flabel("എന്ത്"))
        itemEt = field("ഉദാ: ₹500, Python book, Charger")
        f.addView(itemEt)

        f.addView(flabel("തീയതി"))
        dateTv = dateField(dateMs) { ms -> dateMs = ms; dateTv.text = Store.fmtDate(ms) }
        f.addView(dateTv)

        f.addView(flabel("തിരികെ തീയതി"))
        dueTv = dateField(dueMs) { ms -> dueMs = ms; dueTv.text = Store.fmtDate(ms) }
        f.addView(dueTv)

        f.addView(flabel("കുറിപ്പ് (optional)"))
        noteEt = field("എഴുതൂ…")
        f.addView(noteEt)

        sv.addView(f)
        root.addView(sv)

        val save = Ui.tv(this, "Save", 14f, R.color.amber_dark, true).apply {
            setBackgroundResource(R.drawable.btn_amber)
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(this@AddActivity, 13), 0, Ui.dp(this@AddActivity, 13))
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val m = Ui.dp(this@AddActivity, 16)
            lp.setMargins(m, Ui.dp(this@AddActivity, 8), m, Ui.dp(this@AddActivity, 16))
            layoutParams = lp
            isClickable = true; isFocusable = true
            setOnClickListener { save() }
        }
        root.addView(save)

        setContentView(root)
    }

    private fun togBtn(text: String, on: Boolean, fn: () -> Unit): TextView =
        Ui.tv(this, text, 12.5f, if (on) R.color.amber_dark else R.color.grey, on).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(0, Ui.dp(this@AddActivity, 9), 0, Ui.dp(this@AddActivity, 9))
            isClickable = true; isFocusable = true
            setOnClickListener { fn() }
        }

    private fun paintTog() {
        for ((v, on) in listOf(togA to isLent, togB to !isLent)) {
            v.setBackgroundResource(if (on) R.drawable.btn_amber else 0)
            v.setTextColor(Ui.color(this, if (on) R.color.amber_dark else R.color.grey))
        }
    }

    private fun flabel(s: String): TextView =
        Ui.tv(this, s, 11f, R.color.grey, true).apply {
            setPadding(0, Ui.dp(this@AddActivity, 12), 0, Ui.dp(this@AddActivity, 6))
        }

    private fun field(hint: String): EditText =
        EditText(this).apply {
            this.hint = hint
            setHintTextColor(Ui.color(this@AddActivity, R.color.grey2))
            setTextColor(Ui.color(this@AddActivity, R.color.white))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setBackgroundResource(R.drawable.field)
            val p = Ui.dp(this@AddActivity, 12)
            setPadding(p, Ui.dp(this@AddActivity, 11), p, Ui.dp(this@AddActivity, 11))
            isSingleLine = true
        }

    private fun dateField(ms: Long, onPick: (Long) -> Unit): TextView =
        Ui.tv(this, Store.fmtDate(ms), 13f, R.color.white).apply {
            setBackgroundResource(R.drawable.field)
            val p = Ui.dp(this@AddActivity, 12)
            setPadding(p, Ui.dp(this@AddActivity, 11), p, Ui.dp(this@AddActivity, 11))
            isClickable = true; isFocusable = true
            setOnClickListener {
                val c = Calendar.getInstance()
                c.timeInMillis = ms
                DatePickerDialog(
                    this@AddActivity,
                    { _, y, mo, d ->
                        val nc = Calendar.getInstance()
                        nc.set(y, mo, d, 9, 0, 0)
                        nc.set(Calendar.MILLISECOND, 0)
                        onPick(nc.timeInMillis)
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

    private fun save() {
        val person = personEt.text.toString().trim()
        val item = itemEt.text.toString().trim()
        if (person.isEmpty() || item.isEmpty()) {
            Toast.makeText(this, "ആരുടെ പേരും എന്താണെന്നും എഴുതൂ", Toast.LENGTH_SHORT).show()
            return
        }
        Store.add(
            this, if (isLent) "lent" else "borrowed",
            person, item, dateMs, dueMs, noteEt.text.toString()
        )
        Toast.makeText(this, "Saved ✓", Toast.LENGTH_SHORT).show()
        finish()
    }
}
