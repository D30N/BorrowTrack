package com.deon.borrowtrack

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

/**
 * In-app updates via the GitHub Releases API (no JSON file needed).
 * Automatic check: once per day on launch, silent unless an update is found.
 * Manual check: Settings → Check for updates.
 * The update APK always comes from this repo's GitHub releases over HTTPS,
 * and the user always confirms the install in the system UI (never silent).
 */
object UpdateCheck {
    private const val API_URL = "https://api.github.com/repos/D30N/BorrowTrack/releases/latest"
    private const val PREFS = "bt_update_check"
    private const val KEY_LAST_DAY = "last_check_day"
    private const val AUTHORITY = "com.deon.borrowtrack.files"

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /** Automatic daily check: silent on failure / when already latest. */
    fun autoCheck(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE)
        if (prefs.getString(KEY_LAST_DAY, "") == today()) return
        prefs.edit().putString(KEY_LAST_DAY, today()).apply()
        check(activity, manual = false)
    }

    fun check(activity: Activity, manual: Boolean) {
        Thread {
            try {
                val conn = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "BorrowTrack-UpdateCheck")
                    setRequestProperty("Accept", "application/vnd.github+json")
                    connectTimeout = 10000
                    readTimeout = 10000
                }
                if (conn.responseCode != 200) throw Exception("HTTP ${conn.responseCode}")
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                val tag = json.optString("tag_name", "").removePrefix("v").trim()
                val notes = json.optString("body", "").trim()
                var apkUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val a = assets.getJSONObject(i)
                        if (a.optString("name", "").endsWith(".apk")) {
                            apkUrl = a.optString("browser_download_url", "")
                            break
                        }
                    }
                }
                val installed = try {
                    activity.packageManager.getPackageInfo(activity.packageName, 0).versionName ?: ""
                } catch (e: Exception) { "" }
                if (tag.isEmpty() || apkUrl.isEmpty() || !isNewer(tag, installed)) {
                    if (manual) toastOnUi(activity, "You're on the latest version")
                    return@Thread
                }
                activity.runOnUiThread {
                    AlertDialog.Builder(activity)
                        .setTitle("New version $tag available")
                        .setMessage(if (notes.isEmpty()) "An update is available for BorrowTrack." else notes.take(500))
                        .setPositiveButton("Update") { _, _ -> downloadAndInstall(activity, apkUrl, tag) }
                        .setNegativeButton("Later", null)
                        .show()
                }
            } catch (e: Exception) {
                if (manual) toastOnUi(activity, "Couldn't check for updates")
            }
        }.start()
    }

    /** Numeric dot-segment compare: 1.10 > 1.9 must hold (never plain string compare). */
    fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val l = local.split(".").map { it.toIntOrNull() ?: 0 }
        val n = maxOf(r.size, l.size)
        for (i in 0 until n) {
            val rv = r.getOrNull(i) ?: 0
            val lv = l.getOrNull(i) ?: 0
            if (rv != lv) return rv > lv
        }
        return false
    }

    private fun downloadAndInstall(activity: Activity, apkUrl: String, version: String) {
        Thread {
            try {
                val dir = File(activity.cacheDir, "update").apply { mkdirs() }
                val out = File(dir, "BorrowTrack-$version.apk")
                val conn = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "BorrowTrack-UpdateCheck")
                    connectTimeout = 15000
                    readTimeout = 30000
                }
                if (conn.responseCode != 200) throw Exception("HTTP ${conn.responseCode}")
                conn.inputStream.use { input ->
                    FileOutputStream(out).use { output -> input.copyTo(output) }
                }
                val uri: Uri = Uri.parse("content://$AUTHORITY/${out.name}")
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.runOnUiThread { activity.startActivity(intent) }
            } catch (e: Exception) {
                toastOnUi(activity, "Update download failed")
            }
        }.start()
    }

    private fun toastOnUi(activity: Activity, msg: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
