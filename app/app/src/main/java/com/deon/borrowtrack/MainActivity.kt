package com.deon.borrowtrack

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {

    private var tab = "home"
    private var seg = "lent" // lent | borrowed

    private lateinit var listBox: LinearLayout
    private lateinit var segRow: LinearLayout
    private lateinit var statLent: LinearLayout
    private lateinit var statBorrowed: LinearLayout
    private lateinit var navBar: LinearLayout
    private lateinit var titleTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Remind.ensureChannel(this)
        askNotifPerm()
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun askNotifPerm() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.color(this@MainActivity, R.color.bg))
        }

        // App bar
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val p = Ui.dp(this@MainActivity, 14)
            setPadding(p, Ui.dp(this@MainActivity, 10), p, Ui.dp(this@MainActivity, 8))
        }
        val logo = Ui.tv(this, "BorrowTrack", 18f, R.color.white, true)
        val full = android.text.SpannableString("BorrowTrack")
        full.setSpan(
            android.text.style.ForegroundColorSpan(Ui.color(this, R.color.amber)),
            6, 11, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        logo.text = full
        bar.addView(logo)
        bar.addView(Ui.spacer(this))
        val fab = Ui.tv(this, "+", 22f, R.color.amber_dark, true).apply {
            setBackgroundResource(R.drawable.btn_amber)
            gravity = Gravity.CENTER
            val s = Ui.dp(this@MainActivity, 38)
            layoutParams = LinearLayout.LayoutParams(s, s)
            isClickable = true; isFocusable = true
            setOnClickListener {
                startActivity(Intent(this@MainActivity, AddActivity::class.java))
            }
        }
        bar.addView(fab)
        root.addView(bar)

        // Summary stats
        val sum = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val m = Ui.dp(this@MainActivity, 10)
            setPadding(m, 0, m, Ui.dp(this@MainActivity, 8))
        }
        statLent = statCard("തിരികെ കിട്ടാനുള്ളത്")
        statBorrowed = statCard("തിരികെ കൊടുക്കാനുള്ളത്")
        sum.addView(statLent)
        val sp = Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(Ui.dp(this@MainActivity, 10), 1)
        }
        sum.addView(sp)
        sum.addView(statBorrowed)
        root.addView(sum)

        // Segment (home only)
        segRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.card)
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val m = Ui.dp(this@MainActivity, 10)
            lp.setMargins(m, 0, m, Ui.dp(this@MainActivity, 8))
            layoutParams = lp
            setPadding(Ui.dp(this@MainActivity, 3), Ui.dp(this@MainActivity, 3),
                Ui.dp(this@MainActivity, 3), Ui.dp(this@MainActivity, 3))
        }
        root.addView(segRow)

        titleTv = Ui.tv(this, "", 12f, R.color.grey, true).apply {
            val m = Ui.dp(this@MainActivity, 14)
            setPadding(m, Ui.dp(this@MainActivity, 2), 0, Ui.dp(this@MainActivity, 6))
        }
        root.addView(titleTv)

        // List
        val sv = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
            isVerticalScrollBarEnabled = false
        }
        listBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        sv.addView(listBox)
        root.addView(sv)

        // Bottom nav
        navBar = LinearLayout(this)
        navBar.orientation = LinearLayout.HORIZONTAL
        root.addView(navBar)

        setContentView(root)
        showTab("home")
    }

    private fun statCard(label: String): LinearLayout {
        val l = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.card)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            val p = Ui.dp(this@MainActivity, 12)
            setPadding(p, Ui.dp(this@MainActivity, 10), p, Ui.dp(this@MainActivity, 10))
        }
        val n = Ui.tv(this, "0", 20f, R.color.amber, true)
        val lb = Ui.tv(this, label, 10.5f, R.color.grey)
        l.addView(n); l.addView(lb)
        l.setTag(n)
        return l
    }

    private fun setStat(card: LinearLayout, v: Int) {
        (card.getTag() as TextView).text = v.toString()
    }

    private fun showTab(t: String) {
        tab = t
        navBar.removeAllViews()
        navBar.addView(Ui.tabBtn(this, "🏠", "Home", t == "home") { showTab("home") })
        navBar.addView(Ui.tabBtn(this, "🔔", "Reminders", t == "rem") { showTab("rem") })
        navBar.addView(Ui.tabBtn(this, "📜", "History", t == "hist") { showTab("hist") })
        render()
    }

    private fun render() {
        val all = Store.all(this)
        val active = all.filter { !it.returned }
        setStat(statLent, active.count { it.isLent })
        setStat(statBorrowed, active.count { !it.isLent })

        segRow.visibility = if (tab == "home") View.VISIBLE else View.GONE
        if (tab == "home") renderSeg()

        listBox.removeAllViews()
        val list: List<Entry>
        val title: String
        when (tab) {
            "rem" -> {
                title = "Due soon & overdue"
                list = active.filter { it.daysOverdue() > 0 || it.daysLeft() <= 3 }
                    .sortedBy { it.dueMs }
            }
            "hist" -> {
                title = "Returned"
                list = all.filter { it.returned }.sortedByDescending { it.returnedMs }
            }
            else -> {
                title = if (seg == "lent") "ഞാൻ കൊടുത്തത്" else "ഞാൻ വാങ്ങിയത്"
                list = active.filter { (seg == "lent") == it.isLent }
            }
        }
        titleTv.text = title
        if (list.isEmpty()) {
            listBox.addView(Ui.tv(this, emptyHint(), 12f, R.color.grey2).apply {
                gravity = Gravity.CENTER
                setPadding(0, Ui.dp(this@MainActivity, 40), 0, 0)
            })
            return
        }
        for (e in list) listBox.addView(card(e))
    }

    private fun emptyHint(): String = when (tab) {
        "rem" -> "No dues — all clear ✓"
        "hist" -> "Nothing returned yet"
        else -> "No entries — tap + to add"
    }

    private fun renderSeg() {
        segRow.removeAllViews()
        val a = Ui.tv(this, "കൊടുത്തത്", 12f,
            if (seg == "lent") R.color.amber_dark else R.color.grey, seg == "lent").apply {
            setBackgroundResource(if (seg == "lent") R.drawable.btn_amber else 0)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(0, Ui.dp(this@MainActivity, 8), 0, Ui.dp(this@MainActivity, 8))
            isClickable = true; isFocusable = true
            setOnClickListener { seg = "lent"; render() }
        }
        val b = Ui.tv(this, "വാങ്ങിയത്", 12f,
            if (seg == "borrowed") R.color.amber_dark else R.color.grey, seg == "borrowed").apply {
            setBackgroundResource(if (seg == "borrowed") R.drawable.btn_amber else 0)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(0, Ui.dp(this@MainActivity, 8), 0, Ui.dp(this@MainActivity, 8))
            isClickable = true; isFocusable = true
            setOnClickListener { seg = "borrowed"; render() }
        }
        segRow.addView(a); segRow.addView(b)
    }

    private fun card(e: Entry): LinearLayout {
        val c = Ui.card(this)
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val item = Ui.tv(this, e.item, 13.5f, R.color.white, true).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        top.addView(item)
        top.addView(Ui.badge(this, e))
        c.addView(top)
        c.addView(Ui.tv(this, Ui.subLine(e), 11.5f, R.color.grey).apply {
            setPadding(0, Ui.dp(this@MainActivity, 4), 0, 0)
        })
        c.setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java).putExtra("id", e.id))
        }
        if (e.returned) {
            c.setOnLongClickListener {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Delete entry?")
                    .setMessage("${e.item} (${e.person})")
                    .setPositiveButton("Delete") { _, _ ->
                        Store.delete(this, e.id)
                        android.widget.Toast.makeText(this, "Deleted", android.widget.Toast.LENGTH_SHORT).show()
                        render()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }
        return c
    }
}
