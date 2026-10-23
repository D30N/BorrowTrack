package com.deon.borrowtrack

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.color(this@SettingsActivity, R.color.bg))
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val p = Ui.dp(this@SettingsActivity, 14)
            setPadding(p, Ui.dp(this@SettingsActivity, 10), p, Ui.dp(this@SettingsActivity, 8))
        }
        val back = Ui.tv(this, "‹", 26f, R.color.white).apply {
            setPadding(0, 0, Ui.dp(this@SettingsActivity, 12), 0)
            isClickable = true; isFocusable = true
            setOnClickListener { finish() }
        }
        bar.addView(back)
        bar.addView(Ui.tv(this, "Settings", 20f, R.color.white, true))
        root.addView(bar)

        val sv = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val b = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = Ui.dp(this@SettingsActivity, 16)
            setPadding(p, Ui.dp(this@SettingsActivity, 4), p, p)
        }

        // Theme mode
        b.addView(sectionTitle("THEME"))
        val card1 = settingsCard()
        val mode = Theme.mode(this)
        card1.addView(radioRow("Light", mode == "light") {
            Theme.setMode(this, "light"); recreate()
        })
        card1.addView(radioRow("Dark", mode == "dark") {
            Theme.setMode(this, "dark"); recreate()
        })
        card1.addView(radioRow("System default", mode == "system") {
            Theme.setMode(this, "system"); recreate()
        })
        b.addView(card1)

        // Accent colours
        b.addView(sectionTitle("ACCENT COLOUR"))
        val card2 = settingsCard()
        val swRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(this@SettingsActivity, 6), 0, Ui.dp(this@SettingsActivity, 2))
        }
        for (a in Theme.ACCENTS) {
            swRow.addView(swatch(a.key == Theme.accentKey(this), a.color) {
                Theme.setAccent(this, a.key); recreate()
            })
        }
        card2.addView(swRow)
        card2.addView(Ui.tv(this, Theme.accent(this).label, 14f, R.color.grey).apply {
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(this@SettingsActivity, 2), 0, Ui.dp(this@SettingsActivity, 6))
        })
        b.addView(card2)

        // Language
        b.addView(sectionTitle(if (Theme.lang(this) == "en") "LANGUAGE" else "ഭാഷ · LANGUAGE"))
        val card3 = settingsCard()
        val lang = Theme.lang(this)
        card3.addView(radioRow("മലയാളം", lang == "ml") {
            Theme.setLang(this, "ml"); recreate()
        })
        card3.addView(radioRow("English", lang == "en") {
            Theme.setLang(this, "en"); recreate()
        })
        b.addView(card3)

        // Credit
        b.addView(Ui.tv(this, "This app was made by Deon", 13f, R.color.amber).apply {
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            gravity = Gravity.CENTER
            setPadding(0, Ui.dp(this@SettingsActivity, 18), 0, 0)
            setOnClickListener {
                runCatching {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://deepakdeon.github.io")))
                }
            }
        })

        sv.addView(b)
        root.addView(sv)
        setContentView(root)
    }

    private fun sectionTitle(s: String): TextView =
        Ui.tv(this, s, 13f, R.color.grey, true).apply {
            setPadding(0, Ui.dp(this@SettingsActivity, 20), 0, Ui.dp(this@SettingsActivity, 8))
        }

    private fun settingsCard(): LinearLayout {
        val l = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = Theme.cardBg(this@SettingsActivity)
            val p = Ui.dp(this@SettingsActivity, 8)
            setPadding(p, Ui.dp(this@SettingsActivity, 4), p, Ui.dp(this@SettingsActivity, 4))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return l
    }

    private fun radioRow(label: String, checked: Boolean, onClick: () -> Unit): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                Ui.dp(this@SettingsActivity, 8), Ui.dp(this@SettingsActivity, 14),
                Ui.dp(this@SettingsActivity, 8), Ui.dp(this@SettingsActivity, 14)
            )
            isClickable = true; isFocusable = true
            setOnClickListener { onClick() }
        }
        row.addView(Ui.tv(this, if (checked) "◉" else "○", 20f,
            if (checked) R.color.amber else R.color.grey2).apply {
            setPadding(0, 0, Ui.dp(this@SettingsActivity, 12), 0)
        })
        row.addView(Ui.tv(this, label, 16f, R.color.white, checked))
        return row
    }

    private fun swatch(selected: Boolean, color: Int, onClick: () -> Unit): View {
        val wrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            val s = Ui.dp(this@SettingsActivity, 56)
            layoutParams = LinearLayout.LayoutParams(s, s)
            isClickable = true; isFocusable = true
            setOnClickListener { onClick() }
        }
        if (selected) {
            val ring = GradientDrawable()
            ring.shape = GradientDrawable.OVAL
            ring.setStroke(Ui.dp(this, 2), color)
            ring.setColor(android.graphics.Color.TRANSPARENT)
            wrap.background = ring
        }
        val dot = View(this).apply {
            val g = GradientDrawable()
            g.shape = GradientDrawable.OVAL
            g.setColor(color)
            background = g
            val s = Ui.dp(this@SettingsActivity, 40)
            layoutParams = LinearLayout.LayoutParams(s, s)
        }
        wrap.addView(dot)
        return wrap
    }
}
