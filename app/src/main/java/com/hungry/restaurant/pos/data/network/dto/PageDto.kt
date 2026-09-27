package com.hungry.restaurant.pos.data.network.dto

import kotlinx.serialization.Serializable

/**
 * The subset of Spring Data's `Page<T>` JSON shape this app actually reads.
 * `ignoreUnknownKeys` (set on the shared [kotlinx.serialization.json.Json]
 * instance) drops the rest (`pageable`, `sort`, `empty`, …) rather than this
 * class trying to model all of it.
 */
@Serializable
data class PageDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val last: Boolean = true,
)
