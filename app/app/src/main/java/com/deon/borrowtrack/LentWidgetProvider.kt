package com.deon.borrowtrack

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class LentWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_lent)

            val activeLent = Store.all(context).filter { !it.returned && it.isLent }
            views.setTextViewText(R.id.widget_count, "${activeLent.size} pending")

            val intent = Intent(context, WidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("type", "lent")
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list, intent)
            views.setEmptyView(R.id.widget_list, R.id.widget_empty)

            // Header click opens MainActivity
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPi = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, mainPi)

            // Item click template opens DetailActivity
            val detailIntent = Intent(context, DetailActivity::class.java)
            val detailPi = PendingIntent.getActivity(
                context, 0, detailIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, detailPi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
        }

        fun updateAll(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val component = ComponentName(context, LentWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(component)
                for (id in ids) {
                    updateWidget(context, appWidgetManager, id)
                }
            } catch (_: Exception) {}
        }
    }
}
