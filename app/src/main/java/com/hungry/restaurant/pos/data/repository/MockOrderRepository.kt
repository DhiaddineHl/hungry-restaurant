package com.hungry.restaurant.pos.data.repository

import com.hungry.restaurant.pos.data.model.DashboardMetrics
import com.hungry.restaurant.pos.data.model.DeliveryPlatform
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderItem
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.model.OrderType
import com.hungry.restaurant.pos.data.model.PlatformShare
import com.hungry.restaurant.pos.data.model.TopItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [OrderRepository] with realistic seed data. Everything is reactive:
 * mutating an order re-emits [orders], which recomputes [metrics] downstream.
 */
class MockOrderRepository : OrderRepository {

    private val minute = 60_000L
    private val now = System.currentTimeMillis()

    private val _orders = MutableStateFlow(seedOrders())
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    override val metrics: Flow<DashboardMetrics> = _orders.map { computeMetrics(it) }

    override fun orderById(id: String): Order? = _orders.value.firstOrNull { it.id == id }

    override suspend fun updateStatus(orderId: String, status: OrderStatus) {
        _orders.value = _orders.value.map { if (it.id == orderId) it.copy(status = status) else it }
    }

    override suspend fun advance(orderId: String) {
        val next = when (orderById(orderId)?.status) {
            OrderStatus.NEW -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.READY
            OrderStatus.READY -> OrderStatus.COMPLETED
            else -> return
        }
        updateStatus(orderId, next)
    }

    override suspend fun refresh() {
        // No-op for the mock source; a real repository would re-fetch here.
    }

    // --- Metrics -------------------------------------------------------------

    private fun computeMetrics(orders: List<Order>): DashboardMetrics {
        val completed = orders.filter { it.status == OrderStatus.COMPLETED }
        val cancelled = orders.filter { it.status == OrderStatus.CANCELLED }
        val active = orders.filter { it.status.isActive }
        val revenue = completed.sumOf { it.totalCents }
        val avgPrep = if (orders.isEmpty()) 0 else orders.map { it.prepMinutes }.average().toInt()

        val byPlatform = DeliveryPlatform.entries.mapNotNull { platform ->
            val group = orders.filter { it.platform == platform }
            if (group.isEmpty()) null
            else PlatformShare(platform, group.size, group.sumOf { it.totalCents })
        }.sortedByDescending { it.orders }

        val topItems = orders
            .flatMap { it.items }
            .groupBy { it.name }
            .map { (name, lines) ->
                TopItem(name, lines.sumOf { it.quantity }, lines.sumOf { it.lineTotalCents })
            }
            .sortedByDescending { it.soldCount }
            .take(5)

        return DashboardMetrics(
            revenueTodayCents = revenue,
            ordersToday = orders.size,
            avgPrepMinutes = avgPrep,
            activeOrders = active.size,
            completedToday = completed.size,
            cancelledToday = cancelled.size,
            revenueChangePct = 12.4,
            ordersChangePct = 8.1,
            revenueTrendCents = listOf(182_00, 210_00, 176_00, 240_00, 305_00, 412_00, revenue.coerceAtLeast(320_00)),
            trendLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
            platformBreakdown = byPlatform,
            topItems = topItems,
        )
    }

    // --- Seed data -----------------------------------------------------------

    private fun seedOrders(): List<Order> = listOf(
        Order(
            id = "ord_1001",
            shortCode = "A23",
            platform = DeliveryPlatform.UBER_EATS,
            type = OrderType.DELIVERY,
            status = OrderStatus.NEW,
            customerName = "Jordan Blake",
            placedAtMillis = now - 2 * minute,
            prepMinutes = 18,
            deliveryFeeCents = 0,
            taxCents = 214,
            tipCents = 300,
            courierName = "Marco (Uber)",
            deliveryAddress = "42 Elm Street, Apt 3B",
            customerNote = "Please include extra napkins.",
            items = listOf(
                OrderItem("Double Smash Burger", 1, 1295, listOf("No pickles", "Add bacon (+$1.50)")),
                OrderItem("Truffle Fries", 1, 695),
                OrderItem("Craft Cola", 2, 350),
            ),
        ),
        Order(
            id = "ord_1002",
            shortCode = "A24",
            platform = DeliveryPlatform.DOORDASH,
            type = OrderType.PICKUP,
            status = OrderStatus.NEW,
            customerName = "Priya Nair",
            placedAtMillis = now - 4 * minute,
            prepMinutes = 12,
            taxCents = 158,
            items = listOf(
                OrderItem("Margherita Pizza", 1, 1450, listOf("Gluten-free base (+$2.00)")),
                OrderItem("Caesar Salad", 1, 850),
            ),
        ),
        Order(
            id = "ord_1003",
            shortCode = "A25",
            platform = DeliveryPlatform.GRUBHUB,
            type = OrderType.DELIVERY,
            status = OrderStatus.PREPARING,
            customerName = "Sam Whitfield",
            placedAtMillis = now - 9 * minute,
            prepMinutes = 22,
            deliveryFeeCents = 199,
            taxCents = 302,
            tipCents = 400,
            courierName = "Dana (Grubhub)",
            deliveryAddress = "900 Oak Avenue",
            items = listOf(
                OrderItem("Ramen Tonkotsu", 2, 1595, listOf("Extra chashu (+$3.00)", "Soft-boiled egg")),
                OrderItem("Gyoza (6pc)", 1, 725),
            ),
        ),
        Order(
            id = "ord_1004",
            shortCode = "A26",
            platform = DeliveryPlatform.IN_HOUSE,
            type = OrderType.DELIVERY,
            status = OrderStatus.PREPARING,
            customerName = "Elena Ruiz",
            placedAtMillis = now - 14 * minute,
            prepMinutes = 20,
            deliveryFeeCents = 250,
            taxCents = 188,
            tipCents = 250,
            courierName = "In-house rider",
            deliveryAddress = "17 Sunset Blvd",
            customerNote = "Ring the doorbell twice.",
            items = listOf(
                OrderItem("Chicken Tikka Wrap", 2, 995),
                OrderItem("Mango Lassi", 1, 450),
            ),
        ),
        Order(
            id = "ord_1005",
            shortCode = "A27",
            platform = DeliveryPlatform.DELIVEROO,
            type = OrderType.PICKUP,
            status = OrderStatus.READY,
            customerName = "Tom Becker",
            placedAtMillis = now - 21 * minute,
            prepMinutes = 15,
            taxCents = 121,
            items = listOf(
                OrderItem("Pad Thai", 1, 1250, listOf("Peanut allergy — none", "Medium spice")),
                OrderItem("Thai Iced Tea", 1, 420),
            ),
        ),
        Order(
            id = "ord_1006",
            shortCode = "A28",
            platform = DeliveryPlatform.UBER_EATS,
            type = OrderType.DELIVERY,
            status = OrderStatus.READY,
            customerName = "Aisha Khan",
            placedAtMillis = now - 24 * minute,
            prepMinutes = 17,
            deliveryFeeCents = 0,
            taxCents = 205,
            tipCents = 350,
            courierName = "Leo (Uber)",
            deliveryAddress = "5 Birchwood Court",
            items = listOf(
                OrderItem("Falafel Bowl", 1, 1150),
                OrderItem("Hummus & Pita", 1, 650),
                OrderItem("Baklava", 2, 300),
            ),
        ),
        Order(
            id = "ord_1007",
            shortCode = "A18",
            platform = DeliveryPlatform.DOORDASH,
            type = OrderType.DELIVERY,
            status = OrderStatus.COMPLETED,
            customerName = "Chris Doyle",
            placedAtMillis = now - 58 * minute,
            prepMinutes = 19,
            deliveryFeeCents = 199,
            taxCents = 240,
            tipCents = 500,
            courierName = "Rosa (DoorDash)",
            deliveryAddress = "220 Harbor Way",
            items = listOf(
                OrderItem("BBQ Ribs Platter", 1, 2195),
                OrderItem("Cornbread", 2, 350),
            ),
        ),
        Order(
            id = "ord_1008",
            shortCode = "A19",
            platform = DeliveryPlatform.GRUBHUB,
            type = OrderType.PICKUP,
            status = OrderStatus.COMPLETED,
            customerName = "Nina Alvarez",
            placedAtMillis = now - 72 * minute,
            prepMinutes = 14,
            taxCents = 96,
            items = listOf(
                OrderItem("Veggie Sushi Set", 1, 1395),
                OrderItem("Miso Soup", 1, 350),
            ),
        ),
        Order(
            id = "ord_1009",
            shortCode = "A15",
            platform = DeliveryPlatform.DELIVEROO,
            type = OrderType.DELIVERY,
            status = OrderStatus.COMPLETED,
            customerName = "Owen Park",
            placedAtMillis = now - 96 * minute,
            prepMinutes = 21,
            deliveryFeeCents = 150,
            taxCents = 175,
            tipCents = 200,
            courierName = "Mila (Deliveroo)",
            deliveryAddress = "63 Willow Lane",
            items = listOf(
                OrderItem("Steak Frites", 1, 2450, listOf("Medium-rare")),
            ),
        ),
        Order(
            id = "ord_1010",
            shortCode = "A11",
            platform = DeliveryPlatform.IN_HOUSE,
            type = OrderType.PICKUP,
            status = OrderStatus.CANCELLED,
            customerName = "Grace Lee",
            placedAtMillis = now - 110 * minute,
            prepMinutes = 16,
            taxCents = 88,
            customerNote = "Customer cancelled — wrong address.",
            items = listOf(
                OrderItem("Poke Bowl", 1, 1395),
            ),
        ),
    )
}
