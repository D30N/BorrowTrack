package com.deon.borrowtrack

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val type = intent.getStringExtra("type") ?: "lent"
        return WidgetItemFactory(applicationContext, type)
    }
}

class WidgetItemFactory(
    private val context: Context,
    private val type: String
) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<Entry> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val all = Store.all(context)
        items = if (type == "lent") {
            all.filter { !it.returned && it.isLent }
        } else {
            all.filter { !it.returned && !it.isLent }
        }
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= items.size) return null
        val e = items[position]

        val views = RemoteViews(context.packageName, R.layout.widget_item_row)
        views.setTextViewText(R.id.item_title, e.item)
        views.setTextViewText(R.id.item_subtitle, Ui.subLine(e))

        val (statusText, _) = Ui.statusOf(e)
        views.setTextViewText(R.id.item_status, statusText)
        val colorHex = when (statusText) {
            "OVERDUE" -> "#F87171"
            "DUE SOON" -> "#F59E0B"
            else -> "#4ADE80"
        }
        views.setTextColor(R.id.item_status, Color.parseColor(colorHex))

        val fillInIntent = Intent().apply {
            putExtra("id", e.id)
        }
        views.setOnClickFillInIntent(R.id.widget_row_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
