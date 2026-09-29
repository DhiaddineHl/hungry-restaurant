package com.hungry.restaurant.pos.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Format an amount per the design system's money rule: a space as the thousands
 * separator and, for Tunisia's dinar (the platform's default currency), 3 decimals
 * and a "DT" suffix - e.g. `1248.5` + `"TND"` -> "1 248.500 DT". Any other currency
 * falls back to its ISO code at 2 decimals. Pass [withSuffix] = false for list rows
 * that omit the trailing unit, per the design system.
 */
fun Double.asCurrency(currencyCode: String? = null, withSuffix: Boolean = true): String {
    val isTnd = currencyCode.equalsIgnoreCase("TND")
    val decimals = if (isTnd) 3 else 2
    val raw = String.format(Locale.US, "%,.${decimals}f", this)
    // String.format with Locale.US groups with commas; the design system wants spaces.
    val amount = raw.replace(",", " ")
    if (!withSuffix) return amount
    val suffix = if (isTnd) "DT" else currencyCode
    return if (suffix.isNullOrBlank()) amount else "$amount $suffix"
}

private fun String?.equalsIgnoreCase(other: String) = this != null && this.equals(other, ignoreCase = true)

/** Signed percentage for deltas, e.g. 12.5 -> "+12.5%". */
fun Double.asSignedPct(): String {
    val sign = if (this >= 0) "+" else ""
    return "$sign${String.format(Locale.US, "%.1f", this)}%"
}

/**
 * Parses a backend `LocalDateTime.toString()` value (no offset, e.g.
 * "2026-09-27T14:30:00" or "2026-09-27T14:30:00.123") into epoch millis,
 * treating it as the device's default timezone - the same assumption the rest
 * of this app already makes for every other timestamp (see [relativeTime]/
 * [dateTime] in `TimeAgo.kt`, which have only ever worked with device-local
 * millis). Returns null for a blank/unparseable value rather than throwing,
 * so one bad timestamp doesn't take down a whole order card.
 */
fun parseLocalDateTimeMillis(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    val trimmed = value.substringBefore('.')
    return runCatching {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        format.parse(trimmed)?.time
    }.getOrNull()
}

fun Long?.orNow(): Long = this ?: System.currentTimeMillis()
