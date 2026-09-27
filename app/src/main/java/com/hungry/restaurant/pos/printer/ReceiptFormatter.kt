package com.hungry.restaurant.pos.printer

import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.asCurrency
import com.sunmi.peripheral.printer.SunmiPrinterService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lays out an [Order] as an 80mm/58mm thermal receipt using the Sunmi service.
 * Column widths assume the default 58mm head (~32 chars). Nothing here throws on
 * its own — the caller wraps the whole print in a buffer transaction.
 */
internal class ReceiptFormatter(private val svc: SunmiPrinterService) {

    private val timeFmt = SimpleDateFormat("MMM d, HH:mm", Locale.US)

    fun print(order: Order) {
        header(order)
        divider()
        items(order)
        divider()
        totals(order)
        order.comment?.takeIf { it.isNotBlank() }?.let { note ->
            svc.printText("\n", null)
            left(); bold(true)
            svc.printText("Note: ", null)
            bold(false)
            svc.printText("$note\n", null)
        }
        footer(order)
    }

    private fun header(order: Order) {
        center()
        big()
        svc.printText("HUNGRY KITCHEN\n", null)
        normal()
        big()
        svc.printText("#${order.code}\n", null)
        normal()
        svc.printText("${order.customerName}\n", null)
        svc.printText("${timeFmt.format(Date(order.placedAtMillis))}\n", null)
        order.dropoffAddress?.let { svc.printText("$it\n", null) }
    }

    private fun items(order: Order) {
        left()
        order.items.forEach { item ->
            columns(
                "${item.quantity}x ${item.name}",
                item.lineTotal.asCurrency(order.currency),
            )
            item.modifiers.forEach { mod ->
                svc.printText("   - $mod\n", null)
            }
        }
    }

    private fun totals(order: Order) {
        columns("Subtotal", order.subtotal.asCurrency(order.currency))
        if (order.discountTotal > 0) columns("Discount", "-${order.discountTotal.asCurrency(order.currency)}")
        if (order.deliveryFee > 0) columns("Delivery", order.deliveryFee.asCurrency(order.currency))
        if (order.serviceFee > 0) columns("Service", order.serviceFee.asCurrency(order.currency))
        if (order.additionalFees > 0) columns("Additional", order.additionalFees.asCurrency(order.currency))
        bold(true); big()
        columns("TOTAL", order.total.asCurrency(order.currency))
        normal(); bold(false)
    }

    private fun footer(order: Order) {
        svc.printText("\n", null)
        center()
        svc.printText("Thank you!\n", null)
        left()
    }

    // --- style helpers -------------------------------------------------------

    private fun columns(left: String, right: String) {
        svc.printColumnsString(
            arrayOf(left, right),
            intArrayOf(COL_LEFT, COL_RIGHT),
            intArrayOf(ALIGN_LEFT, ALIGN_RIGHT),
            null,
        )
    }

    private fun divider() {
        left(); normal()
        svc.printText("-".repeat(WIDTH) + "\n", null)
    }

    private fun center() = svc.setAlignment(ALIGN_CENTER, null)
    private fun left() = svc.setAlignment(ALIGN_LEFT, null)
    private fun bold(on: Boolean) = svc.sendRAWData(if (on) ESC_BOLD_ON else ESC_BOLD_OFF, null)
    private fun normal() = svc.setFontSize(24f, null)
    private fun big() = svc.setFontSize(32f, null)

    companion object {
        private const val WIDTH = 32
        private const val COL_LEFT = 22
        private const val COL_RIGHT = 10
        private const val ALIGN_LEFT = 0
        private const val ALIGN_CENTER = 1
        private const val ALIGN_RIGHT = 2

        // ESC/POS emphasis toggles for the bold total line.
        private val ESC_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        private val ESC_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    }
}
