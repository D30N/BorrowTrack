package com.deon.borrowtrack

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

/**
 * User theme: Light / Dark / System + accent colour + language (ML / EN).
 * Stored in SharedPreferences; [version] bumps on every change so open
 * screens can recreate themselves.
 */
object Theme {
    private const val PREFS = "borrowtrack_settings"
    private const val K_MODE = "theme_mode"     // system | light | dark
    private const val K_ACCENT = "accent"       // amber | teal | green | purple | pink
    private const val K_LANG = "lang"           // ml | en
    private const val K_VER = "ui_version"

    data class Accent(val key: String, val label: String, val color: Int, val onColor: Int, val chip: Int)

    val ACCENTS = listOf(
        Accent("amber", "Amber", Color.parseColor("#F59E0B"), Color.parseColor("#1C1005"), Color.parseColor("#FDE68A")),
        Accent("teal", "Teal", Color.parseColor("#0D9488"), Color.WHITE, Color.parseColor("#A7E8E0")),
        Accent("green", "Green", Color.parseColor("#16A34A"), Color.WHITE, Color.parseColor("#BBF7D0")),
        Accent("purple", "Purple", Color.parseColor("#7C3AED"), Color.WHITE, Color.parseColor("#DDD6FE")),
        Accent("pink", "Pink", Color.parseColor("#DB2777"), Color.WHITE, Color.parseColor("#FBCFE8"))
    )

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun mode(c: Context): String = prefs(c).getString(K_MODE, "system") ?: "system"
    fun accentKey(c: Context): String = prefs(c).getString(K_ACCENT, "amber") ?: "amber"
    fun lang(c: Context): String = prefs(c).getString(K_LANG, "ml") ?: "ml"
    fun version(c: Context): Int = prefs(c).getInt(K_VER, 0)

    fun accent(c: Context): Accent =
        ACCENTS.firstOrNull { it.key == accentKey(c) } ?: ACCENTS[0]

    fun setMode(c: Context, v: String) { prefs(c).edit().putString(K_MODE, v).apply(); bump(c) }
    fun setAccent(c: Context, v: String) { prefs(c).edit().putString(K_ACCENT, v).apply(); bump(c) }
    fun setLang(c: Context, v: String) { prefs(c).edit().putString(K_LANG, v).apply(); bump(c) }
    private fun bump(c: Context) {
        prefs(c).edit().putInt(K_VER, version(c) + 1).apply()
    }

    fun isDark(c: Context): Boolean = when (mode(c)) {
        "light" -> false
        "dark" -> true
        else -> (c.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    }

    /** Themed replacement for the R.color palette; unknown ids fall through. */
    fun colorFor(c: Context, id: Int): Int {
        val dark = isDark(c)
        return when (id) {
            R.color.bg -> Color.parseColor(if (dark) "#0E0D0B" else "#F6F3EC")
            R.color.surface -> Color.parseColor(if (dark) "#17140F" else "#FFFFFF")
            R.color.border -> Color.parseColor(if (dark) "#2A241A" else "#E0D8C4")
            R.color.white -> Color.parseColor(if (dark) "#F3EFE8" else "#201A15")
            R.color.grey -> Color.parseColor(if (dark) "#A89F8D" else "#6E6455")
            R.color.grey2 -> Color.parseColor(if (dark) "#6E6656" else "#A79C86")
            R.color.amber -> accent(c).color
            R.color.amber_dark -> accent(c).onColor
            R.color.red -> Color.parseColor(if (dark) "#F87171" else "#DC2626")
            R.color.green -> Color.parseColor(if (dark) "#4ADE80" else "#16A34A")
            else -> androidx.core.content.ContextCompat.getColor(c, id)
        }
    }

    private fun rounded(c: Context, fill: Int, radiusDp: Int, stroke: Int? = null): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(fill)
        g.cornerRadius = Ui.dp(c, radiusDp).toFloat()
        if (stroke != null) g.setStroke(Ui.dp(c, 1), stroke)
        return g
    }

    fun cardBg(c: Context): GradientDrawable =
        rounded(c, colorFor(c, R.color.surface), 14, colorFor(c, R.color.border))

    fun accentBg(c: Context): GradientDrawable =
        rounded(c, accent(c).color, 14)

    fun fieldBg(c: Context): GradientDrawable =
        rounded(c, colorFor(c, R.color.surface), 12, colorFor(c, R.color.border))

    fun chipBg(c: Context): GradientDrawable =
        rounded(c, accent(c).chip, 10)

    fun circleAccentBg(c: Context): GradientDrawable {
        val g = GradientDrawable()
        g.shape = GradientDrawable.OVAL
        g.setColor(accent(c).color)
        return g
    }
}

/** All user-facing strings in Malayalam (default) and English. */
object L {
    private fun en(c: Context) = Theme.lang(c) == "en"
    private fun t(c: Context, ml: String, enT: String) = if (en(c)) enT else ml

    fun statLent(c: Context) = t(c, "തിരികെ കിട്ടാനുള്ളത്", "To get back")
    fun statBorrowed(c: Context) = t(c, "തിരികെ കൊടുക്കാനുള്ളത്", "To give back")
    fun segLent(c: Context) = t(c, "കൊടുത്തത്", "Lent")
    fun segBorrowed(c: Context) = t(c, "വാങ്ങിയത്", "Borrowed")
    fun titleLent(c: Context) = t(c, "ഞാൻ കൊടുത്തത്", "I lent")
    fun titleBorrowed(c: Context) = t(c, "ഞാൻ വാങ്ങിയത്", "I borrowed")
    fun titleRem(c: Context) = t(c, "കഴിഞ്ഞതും അടുത്തതും", "Due soon & overdue")
    fun titleHist(c: Context) = t(c, "തിരികെ കിട്ടിയവ", "Returned")
    fun emptyHome(c: Context) = t(c, "ഒന്നുമില്ല — + അമർത്തി ചേർക്കൂ", "No entries — tap + to add")
    fun emptyRem(c: Context) = t(c, "കുടിശ്ശികയില്ല — എല്ലാം clear ✓", "No dues — all clear ✓")
    fun emptyHist(c: Context) = t(c, "ഒന്നും തിരികെ കിട്ടിയിട്ടില്ല", "Nothing returned yet")

    fun newEntry(c: Context) = t(c, "പുതിയ entry", "New entry")
    fun who(c: Context) = t(c, "ആര്", "Who")
    fun what(c: Context) = t(c, "എന്ത്", "What")
    fun dateL(c: Context) = t(c, "തീയതി", "Date")
    fun dueLent(c: Context) = t(c, "തിരികെ വേണ്ട തീയതി", "Due back by")
    fun dueBorrowed(c: Context) = t(c, "തിരികെ കൊടുക്കേണ്ട തീയതി", "Return by")
    fun dueGeneric(c: Context) = t(c, "തിരികെ തീയതി", "Return date")
    fun noteL(c: Context) = t(c, "കുറിപ്പ്", "Note")
    fun noteOpt(c: Context) = t(c, "കുറിപ്പ് (optional)", "Note (optional)")
    fun egPerson(c: Context) = t(c, "ഉദാ: Arun", "e.g. Arun")
    fun egItem(c: Context) = t(c, "ഉദാ: ₹500, Python book, Charger", "e.g. ₹500, Python book, Charger")
    fun writeHint(c: Context) = t(c, "എഴുതൂ…", "Write…")
    fun fillToast(c: Context) = t(c, "ആരുടെ പേരും എന്താണെന്നും എഴുതൂ", "Please enter who and what")
    fun iLent(c: Context) = t(c, "ഞാൻ കൊടുത്തു", "I lent")
    fun iBorrowed(c: Context) = t(c, "ഞാൻ വാങ്ങി", "I borrowed")

    fun givenTo(c: Context, p: String) = t(c, "$p-ക്ക് കൊടുത്തത്", "Given to $p")
    fun takenFrom(c: Context, p: String) = t(c, "$p-ൽ നിന്ന് വാങ്ങിയത്", "Taken from $p")
    fun returnedVal(c: Context, d: String) = t(c, "തിരികെ കിട്ടി · $d", "Returned · $d")
    fun gotIt(c: Context) = t(c, "തിരികെ കിട്ടി ✓", "Got it back ✓")
    fun gaveBack(c: Context) = t(c, "തിരികെ കൊടുത്തു ✓", "Returned ✓")
    fun tooMuch(c: Context, m: String) = t(c, "തുക കൂടുതലാണ്! (Max: $m)", "Amount is too much! (Max: $m)")
    fun validAmt(c: Context) = t(c, "Valid amount എഴുതൂ", "Enter a valid amount")
    fun remindLent(c: Context, p: String, i: String) =
        t(c, "Hi $p! $i തിരികെ തരാമോ? 🙂", "Hi $p! Can you return $i? 🙂")
    fun remindBorrowed(c: Context, p: String, i: String) =
        t(c, "Hi $p! $i തിരികെ തരാം 👍", "Hi $p! I'll return $i 👍")
    fun notifLent(c: Context, p: String, i: String) =
        t(c, "$p തന്ന $i തിരികെ വാങ്ങാൻ സമയമായി", "Time to collect $i from $p")
    fun notifBorrowed(c: Context, p: String, i: String) =
        t(c, "$p-ന് കൊടുക്കാനുള്ള $i-ന്റെ സമയമായി", "Time to return $i to $p")
}
