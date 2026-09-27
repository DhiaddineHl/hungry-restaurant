package com.hungry.restaurant.pos.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyDto(
    val isoCode: String? = null,
    val symbol: String? = null,
)

/** Mirrors `PriceOutputData` — only a product's base price is read here. */
@Serializable
data class PriceDto(
    val amount: Double? = null,
    val currency: CurrencyDto? = null,
)

@Serializable
data class CategoryDto(
    val id: String? = null,
    val name: String? = null,
)

/** Mirrors `ProductOutputData` — a menu item as the POS app's Menu screen shows it. */
@Serializable
data class ProductDto(
    val id: String,
    val code: String? = null,
    val name: String? = null,
    val description: String? = null,
    val subcategories: List<CategoryDto> = emptyList(),
    val prices: List<PriceDto> = emptyList(),
    val available: Boolean = true,
    val unavailableReason: String? = null,
    val prepTimeMinutes: Int? = null,
)

/** Body for `PATCH /restaurants/me/menu/{id}/availability`. */
@Serializable
data class AvailabilityRequestDto(
    val available: Boolean? = null,
    val unavailableReason: String? = null,
    val prepTimeMinutes: Int? = null,
)
