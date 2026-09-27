package com.hungry.restaurant.pos.data.model

/**
 * An order placed through Hungry's own customer app - the only source of
 * orders this backend models (no multi-marketplace aggregation: an earlier
 * version of this screen showed Uber Eats/DoorDash/etc. chips, which had no
 * backing data anywhere and were dropped when this was wired to the real API).
 *
 * Money fields are the exact amounts `OrderOutputData` computed server-side
 * (the same figures a receipt bills), never recomputed on-device.
 */
data class Order(
    val id: String,
    /** Short human code shown on tickets, e.g. "ORD-0231". */
    val code: String,
    val status: OrderStatus,
    val customerName: String,
    val dropoffAddress: String?,
    /** Special instructions / allergy note the customer left at checkout. */
    val comment: String?,
    val items: List<OrderItem>,
    val subtotal: Double,
    val discountTotal: Double,
    val deliveryFee: Double,
    val serviceFee: Double,
    val additionalFees: Double,
    val total: Double,
    val currency: String?,
    val placedAtMillis: Long,
    val confirmedAtMillis: Long?,
    val preparingAtMillis: Long?,
    val readyAtMillis: Long?,
    val finishedAtMillis: Long?,
    val cancelledAtMillis: Long?,
    val cancelReason: String?,
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
}

data class OrderItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    /** Selected options / add-ons, e.g. "No onions", "Extra cheese". */
    val modifiers: List<String> = emptyList(),
)

/** Mirrors the backend's `OrderStatus` enum exactly - names must match its JSON values. */
enum class OrderStatus {
    /** Just placed, awaiting the restaurant to accept or reject it. */
    CREATED,

    /** Accepted; about to move to PREPARING (this app calls prepare() right after confirm()). */
    CONFIRMED,

    /** Declined outright, before any preparation started. */
    REJECTED,

    /** Accepted and being cooked. */
    PREPARING,

    /** Cooked and waiting for pickup/handover. */
    READY,

    /** Handed off; done from the kitchen's side. */
    FINISHED,

    /** Called off after being accepted. */
    CANCELLED;

    /** Orders the kitchen is actively working - drives the Active Orders board. */
    val isActive: Boolean get() = this == CREATED || this == CONFIRMED || this == PREPARING || this == READY

    /** Terminal, past orders - drives the History screen. */
    val isPast: Boolean get() = this == FINISHED || this == REJECTED || this == CANCELLED
}
