package com.hungry.restaurant.pos.ui.screens.active

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.PrintDisabled
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.repository.ConnectionState
import com.hungry.restaurant.pos.printer.SunmiPrinter
import com.hungry.restaurant.pos.ui.components.ActiveOrderCard
import com.hungry.restaurant.pos.ui.components.Avatar
import com.hungry.restaurant.pos.ui.components.Banner
import com.hungry.restaurant.pos.ui.components.EmptyState
import com.hungry.restaurant.pos.ui.components.SecondaryButton
import com.hungry.restaurant.pos.ui.components.StatusPill
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Full width, screen minus the 16dp margin on each side - no next-column peek per the
// updated design system ("§5 Board columns"). The swipe affordance is now a text hint
// below the last card instead.
private val COLUMN_WIDTH = 328.dp
private val COLUMN_GAP = 12.dp

@Composable
fun ActiveOrdersScreen(
    contentPadding: PaddingValues,
    onOrderClick: (String) -> Unit,
    onMessage: (String) -> Unit,
    viewModel: ActiveOrdersViewModel = viewModel(factory = ActiveOrdersViewModel.Factory),
) {
    val c = Hungry.colors
    val board by viewModel.board.collectAsStateWithLifecycle()
    val restaurant by viewModel.restaurant.collectAsStateWithLifecycle()
    val printerStatus by viewModel.printerStatus.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val defaultPrepMinutes by viewModel.defaultPrepTimeMinutes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { onMessage(it) }
    }

    val pagerState = rememberPagerState(initialPage = selectedTab.ordinal) { BoardTab.entries.size }
    val scope = rememberCoroutineScope()

    // Two-way sync between the tab strip and the swipeable columns.
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab.ordinal) {
            pagerState.animateScrollToPage(selectedTab.ordinal)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        val tab = BoardTab.entries[pagerState.currentPage]
        if (tab != selectedTab) viewModel.selectTab(tab)
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding() + 12.dp))
            Header(
                restaurantName = restaurant?.name ?: "Orders",
                logoUrl = restaurant?.logoUrl,
                total = board.total,
                defaultPrepMinutes = defaultPrepMinutes,
                acceptingOrders = restaurant?.acceptingOrders ?: true,
                online = connection.isOnline,
                onToggleAcceptingOrders = viewModel::toggleAcceptingOrders,
                printerStatus = printerStatus,
                onReconnect = viewModel::reconnectPrinter,
            )
            Spacer(Modifier.height(14.dp))
            BoardTabStrip(
                selected = selectedTab,
                counts = Triple(board.incoming.size, board.preparing.size, board.ready.size),
                onSelect = { tab -> scope.launch { pagerState.animateScrollToPage(tab.ordinal) } },
            )
        }

        if (!connection.isOnline) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OfflineBanner(onRetry = viewModel::retryNow)
            }
        }

        Spacer(Modifier.height(14.dp))

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(COLUMN_WIDTH),
            pageSpacing = COLUMN_GAP,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            val tab = BoardTab.entries[page]
            val visibleOrders = when (tab) {
                BoardTab.NEW -> board.incoming
                BoardTab.PREPARING -> board.preparing
                BoardTab.READY -> board.ready
            }
            BoardColumn(
                tab = tab,
                orders = visibleOrders,
                online = connection.isOnline,
                defaultPrepMinutes = defaultPrepMinutes ?: 20,
                bottomPadding = contentPadding.calculateBottomPadding() + 16.dp,
                onOrderClick = onOrderClick,
                onAccept = { viewModel.accept(it) },
                onReject = { viewModel.reject(it) },
                onMarkReady = { viewModel.markReady(it) },
                onPrint = { viewModel.print(it) },
                onTestAlertSound = viewModel::testAlertSound,
                onSwipeNext = { scope.launch { pagerState.animateScrollToPage(tab.ordinal + 1) } },
            )
        }
    }
}

@Composable
private fun Header(
    restaurantName: String,
    logoUrl: String?,
    total: Int,
    defaultPrepMinutes: Int?,
    acceptingOrders: Boolean,
    online: Boolean,
    onToggleAcceptingOrders: () -> Unit,
    printerStatus: SunmiPrinter.Status,
    onReconnect: () -> Unit,
) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (!logoUrl.isNullOrBlank()) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(36.dp).clip(CircleShape).border(1.dp, c.outline, CircleShape),
            )
        } else {
            Avatar(restaurantName, size = 36.dp, isCurrentUser = false)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            androidx.compose.material3.Text(restaurantName, style = type.headline, color = c.ink)
            val subtitle = if (total > 0 && defaultPrepMinutes != null) "$total active · Avg $defaultPrepMinutes min" else "$total active"
            androidx.compose.material3.Text(subtitle, style = type.body, color = c.inkMuted)
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.clickable(enabled = online, onClick = onToggleAcceptingOrders)) {
            when {
                !online -> StatusPill("Offline", fg = c.danger, bg = c.dangerSoft)
                acceptingOrders -> StatusPill("Open", fg = c.success, bg = c.successSoft)
                else -> StatusPill("Closed", fg = c.danger, bg = c.dangerSoft)
            }
        }
        Spacer(Modifier.width(8.dp))
        PrinterButton(printerStatus, onReconnect)
    }
}

@Composable
private fun PrinterButton(status: SunmiPrinter.Status, onReconnect: () -> Unit) {
    val c = Hungry.colors
    val connected = status == SunmiPrinter.Status.CONNECTED
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(c.surface)
            .border(1.dp, c.outline, CircleShape)
            .clickable(onClick = onReconnect),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (connected) Icons.Outlined.Print else Icons.Outlined.PrintDisabled,
            contentDescription = "Printer",
            tint = c.ink,
            modifier = Modifier.size(18.dp),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(9.dp)
                .clip(CircleShape)
                .background(if (connected) c.success else c.danger),
        )
    }
}

@Composable
private fun BoardTabStrip(selected: BoardTab, counts: Triple<Int, Int, Int>, onSelect: (BoardTab) -> Unit) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(c.surfaceSunken)
            .border(1.dp, c.outline, RoundedCornerShape(14.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TabCell("New", counts.first, c.primary, c.onPrimary, selected == BoardTab.NEW, Modifier.weight(1f)) { onSelect(BoardTab.NEW) }
        TabCell("Preparing", counts.second, c.infoSoft, c.info, selected == BoardTab.PREPARING, Modifier.weight(1f)) { onSelect(BoardTab.PREPARING) }
        TabCell("Ready", counts.third, c.successSoft, c.success, selected == BoardTab.READY, Modifier.weight(1f)) { onSelect(BoardTab.READY) }
    }
}

@Composable
private fun TabCell(
    label: String,
    count: Int,
    badgeBg: androidx.compose.ui.graphics.Color,
    badgeFg: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(
        modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(11.dp))
            .then(if (selected) Modifier.background(c.surface) else Modifier)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Text(
            label,
            style = type.label,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = if (selected) c.ink else c.inkMuted,
        )
        if (count > 0) {
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier.size(20.dp).clip(CircleShape).background(badgeBg),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Text(count.toString(), style = type.badge, color = badgeFg)
            }
        }
    }
}

@Composable
private fun OfflineBanner(onRetry: () -> Unit) {
    val c = Hungry.colors
    var secondsLeft by remember { mutableIntStateOf(5) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            secondsLeft -= 1
            if (secondsLeft <= 0) {
                onRetry()
                secondsLeft = 5
            }
        }
    }
    Banner(
        icon = Icons.Outlined.WifiOff,
        title = "No internet connection",
        body = "New orders can't reach you. Retrying in $secondsLeft s…",
        fg = c.danger,
        bg = c.dangerSoft,
        actionLabel = "Retry",
        onAction = { onRetry(); secondsLeft = 5 },
    )
}

@Composable
private fun BoardColumn(
    tab: BoardTab,
    orders: List<Order>,
    online: Boolean,
    defaultPrepMinutes: Int,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onOrderClick: (String) -> Unit,
    onAccept: (Order) -> Unit,
    onReject: (Order) -> Unit,
    onMarkReady: (Order) -> Unit,
    onPrint: (Order) -> Unit,
    onTestAlertSound: () -> Unit,
    onSwipeNext: () -> Unit,
) {
    if (orders.isEmpty()) {
        if (tab == BoardTab.NEW) {
            EmptyState(
                icon = Icons.Outlined.Inbox,
                title = "All caught up",
                body = "New orders will ring and show up here. Keep the terminal awake and the volume up.",
                actionLabel = "Test the alert sound",
                onAction = onTestAlertSound,
                iconWellColor = Hungry.colors.surface,
                modifier = Modifier.padding(top = 48.dp),
            )
        } else {
            EmptyState(
                icon = Icons.Outlined.Inbox,
                title = if (tab == BoardTab.PREPARING) "Nothing preparing" else "Nothing ready",
                body = if (tab == BoardTab.PREPARING) "Accepted orders show up here." else "Orders ready for pickup show up here.",
                iconWellColor = Hungry.colors.surface,
                modifier = Modifier.padding(top = 48.dp),
            )
        }
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = bottomPadding),
    ) {
        items(orders, key = { it.id }) { order ->
            Box(if (!online) Modifier.alpha(0.55f) else Modifier) {
                ActiveOrderCard(
                    order = order,
                    onClick = { if (online) onOrderClick(order.id) },
                    defaultPrepMinutes = defaultPrepMinutes,
                    onAccept = if (online) { { onAccept(order) } } else null,
                    onReject = if (online) { { onReject(order) } } else null,
                    onMarkReady = if (online) { { onMarkReady(order) } } else null,
                    onPrint = if (online) { { onPrint(order) } } else null,
                )
            }
        }
        // "No peek" board columns (§5): a text hint replaces the old peeking-next-column
        // affordance. The last tab (Ready) has nothing after it, so it gets no hint.
        tab.next()?.let { next ->
            item {
                androidx.compose.material3.Text(
                    "Swipe for ${next.label} →",
                    style = Hungry.type.label,
                    color = Hungry.colors.inkMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSwipeNext)
                        .padding(top = 4.dp, bottom = 8.dp),
                )
            }
        }
    }
}
