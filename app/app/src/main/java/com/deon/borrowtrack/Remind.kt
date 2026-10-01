package com.deon.borrowtrack

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

object Remind {
    const val CH = "due_reminders"

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CH) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(CH, "Due reminders", NotificationManager.IMPORTANCE_HIGH)
                )
            }
        }
    }

    /** Fires at 9:00 AM on the due date. */
    fun schedule(ctx: Context, e: Entry) {
        try {
            val cal = Calendar.getInstance()
            cal.timeInMillis = e.dueMs
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            var whenMs = cal.timeInMillis
            if (whenMs < System.currentTimeMillis() + 60_000) {
                // due date already passed today — remind in a minute
                whenMs = System.currentTimeMillis() + 60_000
            }
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) return
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMs, pending(ctx, e))
        } catch (_: Exception) {
        }
    }

    fun cancel(ctx: Context, entryId: String) {
        try {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pending(ctx, entryId))
        } catch (_: Exception) {
        }
    }

    private fun pending(ctx: Context, e: Entry): PendingIntent = pending(ctx, e.id)

    private fun pending(ctx: Context, entryId: String): PendingIntent {
        val i = Intent(ctx, ReminderReceiver::class.java).putExtra("id", entryId)
        return PendingIntent.getBroadcast(
            ctx, entryId.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val e = Store.byId(ctx, id) ?: return
        if (e.returned) return
        Remind.ensureChannel(ctx)
        val text = if (e.isLent)
            "${e.person} തന്ന ${e.item} തിരികെ വാങ്ങാൻ സമയമായി"
        else
            "${e.person}-ന് കൊടുക്കാനുള്ള ${e.item}-ന്റെ സമയമായി"
        val open = Intent(ctx, DetailActivity::class.java).putExtra("id", e.id)
        val pi = PendingIntent.getActivity(
            ctx, e.id.hashCode(), open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(ctx, Remind.CH)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("BorrowTrack reminder")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            nm.notify(e.id.hashCode(), n)
        } catch (_: Exception) {
        }
    }
}
