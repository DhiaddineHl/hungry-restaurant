package com.hungry.restaurant.pos.data.model

/**
 * A single delivery/pickup order surfaced from one of the delivery platforms.
 *
 * Money values are stored in whole cents to avoid floating-point rounding on
 * receipts and totals. Use [asCurrency] for display.
 */
data class Order(
    val id: String,
    /** Short human code shown on tickets, e.g. "A23". */
    val shortCode: String,
    val platform: DeliveryPlatform,
    val type: OrderType,
    val status: OrderStatus,
    val customerName: String,
    val items: List<OrderItem>,
    val placedAtMillis: Long,
    /** Promised ready/handover time, minutes from [placedAtMillis]. */
    val prepMinutes: Int,
    val deliveryFeeCents: Int = 0,
    val taxCents: Int = 0,
    val tipCents: Int = 0,
    val courierName: String? = null,
    val deliveryAddress: String? = null,
    val customerNote: String? = null,
) {
    val subtotalCents: Int get() = items.sumOf { it.lineTotalCents }
    val totalCents: Int get() = subtotalCents + taxCents + deliveryFeeCents + tipCents
    val itemCount: Int get() = items.sumOf { it.quantity }
}

data class OrderItem(
    val name: String,
    val quantity: Int,
    val unitPriceCents: Int,
    /** Selected options / add-ons, e.g. "No onions", "Extra cheese (+$1.50)". */
    val modifiers: List<String> = emptyList(),
    val note: String? = null,
) {
    val lineTotalCents: Int get() = unitPriceCents * quantity
}

enum class OrderType { DELIVERY, PICKUP }

enum class OrderStatus {
    /** Just arrived, awaiting the restaurant to accept. */
    NEW,

    /** Accepted and being cooked. */
    PREPARING,

    /** Cooked and waiting for the courier / customer. */
    READY,

    /** Handed to courier / customer; done from the kitchen's side. */
    COMPLETED,

    CANCELLED;

    /** Orders the kitchen is actively working — drives the "Active Orders" screen. */
    val isActive: Boolean get() = this == NEW || this == PREPARING || this == READY
}

/**
 * Delivery marketplaces the restaurant receives orders from. [brandHex] is used
 * by the UI layer to tint the platform chip so staff recognize the source fast.
 */
enum class DeliveryPlatform(val displayName: String, val brandHex: Long) {
    UBER_EATS("Uber Eats", 0xFF06C167),
    DOORDASH("DoorDash", 0xFFFF3008),
    GRUBHUB("Grubhub", 0xFFF63440),
    DELIVEROO("Deliveroo", 0xFF00CCBC),
    JUST_EAT("Just Eat", 0xFFFF8000),
    IN_HOUSE("Hungry Direct", 0xFFFF5A1F),
}
