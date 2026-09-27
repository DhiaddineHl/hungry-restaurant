package com.hungry.restaurant.pos.data.network

import com.hungry.restaurant.pos.data.model.MenuItem
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderItem
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.model.PosSettings
import com.hungry.restaurant.pos.data.model.RestaurantProfile
import com.hungry.restaurant.pos.data.model.StaffMember
import com.hungry.restaurant.pos.data.model.StatsPeriod
import com.hungry.restaurant.pos.data.model.StatsSummary
import com.hungry.restaurant.pos.data.model.TopSellingItem
import com.hungry.restaurant.pos.data.model.orNow
import com.hungry.restaurant.pos.data.model.parseLocalDateTimeMillis
import com.hungry.restaurant.pos.data.model.toStaffRole
import com.hungry.restaurant.pos.data.network.dto.OrderDto
import com.hungry.restaurant.pos.data.network.dto.OrderItemDto
import com.hungry.restaurant.pos.data.network.dto.PosSettingsDto
import com.hungry.restaurant.pos.data.network.dto.ProductDto
import com.hungry.restaurant.pos.data.network.dto.RestaurantDto
import com.hungry.restaurant.pos.data.network.dto.StaffDto
import com.hungry.restaurant.pos.data.network.dto.StatsDto

fun OrderDto.toDomain(): Order = Order(
    id = id,
    code = code ?: id.take(8),
    status = status?.let { runCatching { OrderStatus.valueOf(it) }.getOrNull() } ?: OrderStatus.CREATED,
    customerName = customerFullName ?: "Customer",
    dropoffAddress = dropoffAddress,
    comment = comment,
    items = items.map { it.toDomain() },
    subtotal = subtotal ?: 0.0,
    discountTotal = discountTotal ?: 0.0,
    deliveryFee = deliveryFee ?: 0.0,
    serviceFee = serviceFee ?: 0.0,
    additionalFees = additionalFees ?: 0.0,
    total = total ?: 0.0,
    currency = currency,
    placedAtMillis = parseLocalDateTimeMillis(createdAt).orNow(),
    confirmedAtMillis = parseLocalDateTimeMillis(confirmedAt),
    preparingAtMillis = parseLocalDateTimeMillis(preparingAt),
    readyAtMillis = parseLocalDateTimeMillis(readyAt),
    finishedAtMillis = parseLocalDateTimeMillis(finishedAt),
    cancelledAtMillis = parseLocalDateTimeMillis(cancelledAt),
    cancelReason = cancelReason,
)

private fun OrderItemDto.toDomain(): OrderItem {
    val product = product
    return OrderItem(
        name = product?.name ?: "Item",
        quantity = quantity,
        unitPrice = unitPrice ?: product?.unitPrice ?: 0.0,
        lineTotal = total ?: 0.0,
        modifiers = product?.attributes.orEmpty().mapNotNull { it.name },
    )
}

fun RestaurantDto.toDomain(): RestaurantProfile = RestaurantProfile(
    id = id,
    name = name ?: brandName ?: "Restaurant",
    brandName = brandName,
    logoUrl = logoUrl,
    acceptingOrders = acceptingOrders,
)

fun ProductDto.toDomain(): MenuItem = MenuItem(
    id = id,
    name = name ?: "Item",
    category = subcategories.firstOrNull()?.name,
    price = prices.firstOrNull()?.amount,
    currency = prices.firstOrNull()?.currency?.isoCode,
    available = available,
    unavailableReason = unavailableReason,
    prepTimeMinutes = prepTimeMinutes,
)

fun StaffDto.toDomain(): StaffMember = StaffMember(
    id = id,
    name = name,
    initials = initials,
    role = role.toStaffRole(),
)

fun PosSettingsDto.toDomain(): PosSettings = PosSettings(
    autoPrintOnAccept = autoPrintOnAccept,
    ticketCopies = ticketCopies,
    ringUntilAccepted = ringUntilAccepted,
    defaultPrepTimeMinutes = defaultPrepTimeMinutes,
    autoAcceptOrders = autoAcceptOrders,
)

fun StatsDto.toDomain(): StatsSummary = StatsSummary(
    period = runCatching { StatsPeriod.valueOf(period) }.getOrDefault(StatsPeriod.TODAY),
    currency = currency,
    revenue = revenue,
    revenueChangePct = revenueChangePct,
    ordersCount = ordersCount,
    avgPrepMinutes = avgPrepMinutes,
    acceptedPct = acceptedPct,
    cancelledCount = cancelledCount,
    hourlyRevenue = hourlyRevenue,
    topItems = topItems.map { TopSellingItem(it.name, it.quantity) },
)
