package com.questlog.feature.inbox.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun formatRelative(epochMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val diff = nowMillis - epochMillis
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "방금"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}분 전"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}시간 전"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}일 전"
        else -> SimpleDateFormat("M월 d일", Locale.KOREAN).format(Date(epochMillis))
    }
}
