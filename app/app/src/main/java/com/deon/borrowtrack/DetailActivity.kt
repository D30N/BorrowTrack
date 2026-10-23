package com.deon.borrowtrack

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
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
            if (e.isLent) L.givenTo(this, e.person) else L.takenFrom(this, e.person),
            13f, R.color.grey
        ))

        b.addView(flabel(L.dateL(this)))
        b.addView(fval(Store.fmtDate(e.dateMs)))
        b.addView(flabel(if (e.isLent) L.dueLent(this) else L.dueBorrowed(this)))
        b.addView(fval(Store.fmtDate(e.dueMs)))
        if (e.note.isNotBlank()) {
            b.addView(flabel(L.noteL(this)))
            b.addView(fval(e.note))
        }
        if (e.returned) {
            b.addView(flabel("Status"))
            b.addView(fval(L.returnedVal(this, Store.fmtDate(e.returnedMs))))
        }
        sv.addView(b)
        root.addView(sv)

        if (!e.returned) {
            val remind = Ui.tv(this, "Remind", 13f, R.color.white, true).apply {
                background = Theme.fieldBg(this@DetailActivity)
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

            val doneContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = Theme.accentBg(this@DetailActivity)
                val p = Ui.dp(this@DetailActivity, 6)
                setPadding(p, p, p, p)
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val m = Ui.dp(this@DetailActivity, 16)
                lp.setMargins(m, 0, m, Ui.dp(this@DetailActivity, 16))
                layoutParams = lp
            }

            val numMatch = Regex("""\d+(\.\d+)?""").find(e.item)
            val defaultAmt = numMatch?.value ?: e.item

            val amtEt = EditText(this).apply {
                setText(defaultAmt)
                setTextColor(Ui.color(this@DetailActivity, R.color.amber_dark))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = Theme.chipBg(this@DetailActivity)
                val px = Ui.dp(this@DetailActivity, 14)
                val py = Ui.dp(this@DetailActivity, 8)
                setPadding(px, py, px, py)
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                isSingleLine = true
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(Ui.dp(this@DetailActivity, 2), 0, Ui.dp(this@DetailActivity, 8), 0)
                layoutParams = lp
                minWidth = Ui.dp(this@DetailActivity, 70)
            }
            doneContainer.addView(amtEt)

            val btnLabel = Ui.tv(
                this,
                if (e.isLent) L.gotIt(this) else L.gaveBack(this),
                13.5f, R.color.amber_dark, true
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(0, Ui.dp(this@DetailActivity, 8), Ui.dp(this@DetailActivity, 12), Ui.dp(this@DetailActivity, 8))
            }
            doneContainer.addView(btnLabel)

            val handleReturn = {
                val enteredStr = amtEt.text.toString().trim()
                val enteredAmt = enteredStr.toDoubleOrNull()
                val origAmt = numMatch?.value?.toDoubleOrNull()

                if (enteredAmt != null && origAmt != null && origAmt > 0) {
                    val maxFormatted = if (origAmt % 1.0 == 0.0) origAmt.toLong().toString() else origAmt.toString()
                    when {
                        enteredAmt > origAmt -> {
                            Toast.makeText(
                                this@DetailActivity,
                                L.tooMuch(this@DetailActivity, maxFormatted),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        enteredAmt == origAmt -> {
                            Store.markReturned(this@DetailActivity, e.id)
                            Toast.makeText(this@DetailActivity, "Done ✓", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        enteredAmt <= 0 -> {
                            Toast.makeText(this@DetailActivity, L.validAmt(this@DetailActivity), Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Store.updatePartial(this@DetailActivity, e.id, enteredAmt)
                            Toast.makeText(this@DetailActivity, "Updated ✓", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    Store.markReturned(this@DetailActivity, e.id)
                    Toast.makeText(this@DetailActivity, "Done ✓", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            doneContainer.isClickable = true
            doneContainer.isFocusable = true
            doneContainer.setOnClickListener { handleReturn() }
            btnLabel.isClickable = true
            btnLabel.setOnClickListener { handleReturn() }

            root.addView(doneContainer)
        }

        setContentView(root)
    }

    private fun flabel(s: String): TextView =
        Ui.tv(this, s, 11f, R.color.grey, true).apply {
            setPadding(0, Ui.dp(this@DetailActivity, 12), 0, Ui.dp(this@DetailActivity, 6))
        }

    private fun fval(s: String): TextView =
        Ui.tv(this, s, 13f, R.color.white).apply {
            background = Theme.fieldBg(this@DetailActivity)
            val p = Ui.dp(this@DetailActivity, 12)
            setPadding(p, Ui.dp(this@DetailActivity, 11), p, Ui.dp(this@DetailActivity, 11))
        }

    private fun remind(e: Entry) {
        val text = if (e.isLent)
            L.remindLent(this, e.person, e.item)
        else
            L.remindBorrowed(this, e.person, e.item)
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
