package com.questlog.feature.inbox.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.questlog.MainActivity
import com.questlog.R

// v1: 탭 시 앱 실행 (캡처 화면 자동 진입은 MainActivity 가 처리).
// v1.5: 인라인 텍스트 입력 + 캡처 — 그 시점에 EXTRA_FROM_WIDGET 등 도입.
class InboxWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_inbox).apply {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_CAPTURE, true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                /* requestCode = */ 0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }
        appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, views) }
    }

    companion object {
        const val EXTRA_OPEN_CAPTURE = "com.questlog.widget.OPEN_CAPTURE"
    }
}
