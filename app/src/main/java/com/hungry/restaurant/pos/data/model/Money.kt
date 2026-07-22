package com.hungry.restaurant.pos.data.model

import java.util.Locale

/** Format a cents amount as a currency string, e.g. 1250 -> "$12.50". */
fun Int.asCurrency(symbol: String = "$"): String =
    "$symbol${String.format(Locale.US, "%,.2f", this / 100.0)}"

/** Signed percentage for deltas, e.g. 12.5 -> "+12.5%". */
fun Double.asSignedPct(): String {
    val sign = if (this >= 0) "+" else ""
    return "$sign${String.format(Locale.US, "%.1f", this)}%"
}
