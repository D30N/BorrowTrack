package com.deon.borrowtrack

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class DetailActivity : Activity() {

    private var entry: Entry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra("id") ?: ""
        entry = Store.byId(this, id)
        if (entry == null) {
            finish()
            return
        }
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        val id = entry?.id ?: return
        entry = Store.byId(this, id) ?: run { finish(); return }
        buildUi()
    }

    private fun buildUi() {
        val e = entry!!
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.color(this@DetailActivity, R.color.bg))
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val p = Ui.dp(this@DetailActivity, 14)
            setPadding(p, Ui.dp(this@DetailActivity, 10), p, Ui.dp(this@DetailActivity, 8))
        }
        val back = Ui.tv(this, "‹", 26f, R.color.white).apply {
            setPadding(0, 0, Ui.dp(this@DetailActivity, 12), 0)
            isClickable = true; isFocusable = true
            setOnClickListener { finish() }
        }
        bar.addView(back)
        bar.addView(Ui.tv(this, "Details", 16f, R.color.white, true))
        bar.addView(Ui.spacer(this))
        if (!e.returned) {
            val del = Ui.tv(this, "Delete", 12f, R.color.red).apply {
                isClickable = true; isFocusable = true
                setPadding(Ui.dp(this@DetailActivity, 8), Ui.dp(this@DetailActivity, 8),
                    Ui.dp(this@DetailActivity, 4), Ui.dp(this@DetailActivity, 8))
                setOnClickListener { confirmDelete(e) }
            }
            bar.addView(del)
        }
        root.addView(bar)

        val sv = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val b = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = Ui.dp(this@DetailActivity, 16)
            setPadding(p, Ui.dp(this@DetailActivity, 4), p, p)
        }

        b.addView(Ui.badge(this, e).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })
        b.addView(Ui.tv(this, e.item, 19f, R.color.white, true).apply {
            setPadding(0, Ui.dp(this@DetailActivity, 8), 0, Ui.dp(this@DetailActivity, 2))
        })
        b.addView(Ui.tv(this,
            if (e.isLent) "${e.person}-ക്ക് കൊടുത്തത്" else "${e.person}-ൽ നിന്ന് വാങ്ങിയത്",
            13f, R.color.grey
        ))

        b.addView(flabel("തീയതി"))
        b.addView(fval(Store.fmtDate(e.dateMs)))
        b.addView(flabel(if (e.isLent) "തിരികെ വേണ്ട തീയതി" else "തിരികെ കൊടുക്കേണ്ട തീയതി"))
        b.addView(fval(Store.fmtDate(e.dueMs)))
        if (e.note.isNotBlank()) {
            b.addView(flabel("കുറിപ്പ്"))
            b.addView(fval(e.note))
        }
        if (e.returned) {
            b.addView(flabel("Status"))
            b.addView(fval("തിരികെ കിട്ടി · ${Store.fmtDate(e.returnedMs)}"))
        }
        sv.addView(b)
        root.addView(sv)

        if (!e.returned) {
            val remind = Ui.tv(this, "Remind", 13f, R.color.white, true).apply {
                setBackgroundResource(R.drawable.field)
                gravity = Gravity.CENTER
                setPadding(0, Ui.dp(this@DetailActivity, 12), 0, Ui.dp(this@DetailActivity, 12))
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val m = Ui.dp(this@DetailActivity, 16)
                lp.setMargins(m, 0, m, Ui.dp(this@DetailActivity, 8))
                layoutParams = lp
                isClickable = true; isFocusable = true
                setOnClickListener { remind(e) }
            }
            root.addView(remind)

            val done = Ui.tv(this,
                if (e.isLent) "തിരികെ കിട്ടി ✓" else "തിരികെ കൊടുത്തു ✓",
                14f, R.color.amber_dark, true
            ).apply {
                setBackgroundResource(R.drawable.btn_amber)
                gravity = Gravity.CENTER
                setPadding(0, Ui.dp(this@DetailActivity, 13), 0, Ui.dp(this@DetailActivity, 13))
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val m = Ui.dp(this@DetailActivity, 16)
                lp.setMargins(m, 0, m, Ui.dp(this@DetailActivity, 16))
                layoutParams = lp
                isClickable = true; isFocusable = true
                setOnClickListener {
                    Store.markReturned(this@DetailActivity, e.id)
                    Toast.makeText(this@DetailActivity, "Done ✓", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            root.addView(done)
        }

        setContentView(root)
    }

    private fun flabel(s: String): TextView =
        Ui.tv(this, s, 11f, R.color.grey, true).apply {
            setPadding(0, Ui.dp(this@DetailActivity, 12), 0, Ui.dp(this@DetailActivity, 6))
        }

    private fun fval(s: String): TextView =
        Ui.tv(this, s, 13f, R.color.white).apply {
            setBackgroundResource(R.drawable.field)
            val p = Ui.dp(this@DetailActivity, 12)
            setPadding(p, Ui.dp(this@DetailActivity, 11), p, Ui.dp(this@DetailActivity, 11))
        }

    private fun remind(e: Entry) {
        val text = if (e.isLent)
            "Hi ${e.person}! ${e.item} തിരികെ തരാമോ? 🙂"
        else
            "Hi ${e.person}! ${e.item} തിരികെ തരാം 👍"
        val wa = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        try {
            startActivity(wa)
        } catch (_: Exception) {
            val ch = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(ch, "Remind via"))
        }
    }

    private fun confirmDelete(e: Entry) {
        AlertDialog.Builder(this)
            .setTitle("Delete entry?")
            .setMessage("${e.item} (${e.person})")
            .setPositiveButton("Delete") { _, _ ->
                Store.delete(this, e.id)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
