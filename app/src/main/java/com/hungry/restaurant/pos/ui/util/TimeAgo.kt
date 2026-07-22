package com.hungry.restaurant.pos.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** "just now", "6 min ago", "2 hr ago" from an epoch-millis timestamp. */
fun relativeTime(fromMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val diff = (nowMillis - fromMillis).coerceAtLeast(0)
    val minutes = diff / 60_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 1440 -> "${minutes / 60} hr ago"
        else -> "${minutes / 1440} d ago"
    }
}

private val clockFmt = SimpleDateFormat("HH:mm", Locale.US)
private val dateFmt = SimpleDateFormat("MMM d · HH:mm", Locale.US)

fun clockTime(millis: Long): String = clockFmt.format(Date(millis))
fun dateTime(millis: Long): String = dateFmt.format(Date(millis))

/** Minutes remaining until the promised ready time (may be negative if overdue). */
fun minutesUntilReady(placedAtMillis: Long, prepMinutes: Int, nowMillis: Long = System.currentTimeMillis()): Int {
    val readyAt = placedAtMillis + prepMinutes * 60_000L
    return ((readyAt - nowMillis) / 60_000L).toInt()
}
