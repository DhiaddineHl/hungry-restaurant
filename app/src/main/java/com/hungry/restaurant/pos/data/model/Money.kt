package com.hungry.restaurant.pos.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Format an amount with its ISO currency code, e.g. `12.5` + `"TND"` -> "12.500 TND".
 * Tunisia's dinar (the platform's default currency) prints 3 decimals; anything else
 * gets the conventional 2 - matching `CurrencyContext.formatMoney`'s convention on the
 * admin dashboard rather than hardcoding a symbol this app has no reliable source for.
 */
fun Double.asCurrency(currencyCode: String? = null): String {
    val decimals = if (currencyCode.equalsIgnoreCase("TND")) 3 else 2
    val amount = String.format(Locale.US, "%,.${decimals}f", this)
    return if (currencyCode.isNullOrBlank()) amount else "$amount $currencyCode"
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
