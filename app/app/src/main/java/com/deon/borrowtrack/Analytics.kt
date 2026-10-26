package com.deon.borrowtrack

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ---------- Anonymous analytics (ask-me-only) ----------
// Bare Abacus counter increments. NO dashboard page is built and NO
// link is published: the user asks Muse, Muse reads the counter
// backend and reports the numbers.
// Privacy: the hits carry NO identifiers at all — no phone number,
// no device id, no file names, no queries. A random install UUID is
// generated and stored LOCALLY ONLY as a local install marker; it is
// NEVER transmitted anywhere.
fun Context.recordAnalytics() {
    Thread {
        try {
            val prefs = getSharedPreferences("bt_analytics", Context.MODE_PRIVATE)
            if (!prefs.contains("install_uuid")) {
                prefs.edit().putString("install_uuid", UUID.randomUUID().toString()).apply()
            }
            if (!prefs.getBoolean("install_reported", false)) {
                if (hitCounter("installs")) {
                    prefs.edit().putBoolean("install_reported", true).apply()
                }
            }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            if (prefs.getString("last_open_date", "") != today) {
                if (hitCounter("opens")) {
                    prefs.edit().putString("last_open_date", today).apply()
                }
            }
        } catch (_: Exception) {
        }
    }.start()
}

// Fire-and-forget counter increment, ~5s timeout, failures ignored:
// no retries, no UI impact, the app works fully with the backend down.
private fun hitCounter(key: String): Boolean {
    var conn: HttpURLConnection? = null
    try {
        val url = URL("https://abacus.jasoncameron.dev/hit/borrowtrack/" + key)
        conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.requestMethod = "GET"
        val code = conn.responseCode
        try {
            conn.inputStream.close()
        } catch (_: Exception) {
        }
        return code == 200
    } catch (_: Exception) {
        return false
    } finally {
        try {
            conn?.disconnect()
        } catch (_: Exception) {
        }
    }
}
