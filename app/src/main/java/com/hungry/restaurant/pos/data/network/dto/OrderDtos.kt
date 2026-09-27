package com.hungry.restaurant.pos.data.network.dto

import kotlinx.serialization.Serializable

/** Mirrors `OrderedProductAttributOutputData` — a chosen option/modifier on an item. */
@Serializable
data class OrderedProductAttributeDto(
    val id: String? = null,
    val code: String? = null,
    val name: String? = null,
    val price: Double? = null,
)

/** Mirrors `OrderedProductOutputData` — the catalog product snapshot on an order line. */
@Serializable
data class OrderedProductDto(
    val id: String? = null,
    val code: String? = null,
    val name: String? = null,
    val unitPrice: Double? = null,
    val attributes: List<OrderedProductAttributeDto> = emptyList(),
)

/** Mirrors `OrderItemOutputData`. */
@Serializable
data class OrderItemDto(
    val quantity: Int = 0,
    val product: OrderedProductDto? = null,
    val unitPrice: Double? = null,
    val total: Double? = null,
)

/** Mirrors `OrderOutputData.Adjustment`. */
@Serializable
data class OrderAdjustmentDto(
    val type: String? = null,
    val code: String? = null,
    val label: String? = null,
    val amount: Double? = null,
)

/**
 * Mirrors `OrderOutputData` — every field the POS app's board/details/history/
 * stats screens read. Timestamps are the backend's `LocalDateTime.toString()`
 * (no offset, e.g. "2026-09-27T14:30:00"), parsed by [com.hungry.restaurant.pos.data.model.parseLocalDateTimeMillis].
 */
@Serializable
data class OrderDto(
    val id: String,
    val code: String? = null,
    val restaurantId: String? = null,
    val restaurantName: String? = null,
    val customerId: String? = null,
    val customerFullName: String? = null,
    val dropoffAddress: String? = null,
    val status: String? = null,
    val items: List<OrderItemDto> = emptyList(),
    val subtotal: Double? = null,
    val discountTotal: Double? = null,
    val deliveryFee: Double? = null,
    val serviceFee: Double? = null,
    val additionalFees: Double? = null,
    val adjustments: List<OrderAdjustmentDto> = emptyList(),
    val total: Double? = null,
    val currency: String? = null,
    val createdAt: String? = null,
    val comment: String? = null,
    val confirmedAt: String? = null,
    val preparingAt: String? = null,
    val readyAt: String? = null,
    val finishedAt: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null,
)

/** Body for `POST /restaurants/me/orders/{id}/cancel`. */
@Serializable
data class CancelOrderRequestDto(val reason: String? = null)
