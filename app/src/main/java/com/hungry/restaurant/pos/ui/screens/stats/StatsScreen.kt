package com.hungry.restaurant.pos.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.data.model.StatsPeriod
import com.hungry.restaurant.pos.data.model.StatsSummary
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.data.model.asSignedPct
import com.hungry.restaurant.pos.ui.components.EmptyState
import com.hungry.restaurant.pos.ui.components.SegmentedTabs
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius

@Composable
fun StatsScreen(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory),
) {
    val c = Hungry.colors
    val type = Hungry.type
    val period by viewModel.period.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.canvas),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Stats", style = type.headline, color = c.ink)
                SegmentedTabs(
                    options = StatsPeriod.entries,
                    selected = period,
                    onSelect = viewModel::selectPeriod,
                    label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                    small = true,
                    modifier = Modifier.width(228.dp),
                )
            }
        }

        val s = stats
        if (s == null || s.ordersCount == 0) {
            item { RevenueCard(s, emptyState = true) }
            item {
                EmptyState(
                    icon = Icons.Outlined.BarChart,
                    title = "No sales yet ${period.name.lowercase()}",
                    body = "Stats fill in as orders are completed. Check Week for recent results.",
                    actionLabel = if (period != StatsPeriod.WEEK) "View this week" else null,
                    onAction = { viewModel.selectPeriod(StatsPeriod.WEEK) },
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        } else {
            item { RevenueCard(s, emptyState = false) }
            item { KpiGrid(s) }
            item { TopItemsCard(s) }
        }
    }
}

@Composable
private fun RevenueCard(s: StatsSummary?, emptyState: Boolean) {
    val c = Hungry.colors
    val type = Hungry.type
    val isTnd = s?.currency?.equals("TND", ignoreCase = true) != false
    val suffix = if (isTnd) "DT" else s?.currency ?: ""
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HungryRadius.card)
            .background(c.surface)
            .padding(20.dp),
    ) {
        Text("Revenue", style = type.label, color = c.inkMuted)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                (s?.revenue ?: 0.0).asCurrency(s?.currency, withSuffix = false),
                style = type.stat,
                color = if (emptyState) c.inkMuted else c.ink,
            )
            Spacer(Modifier.width(6.dp))
            Text(suffix, style = type.bodyStrong, color = c.inkMuted, modifier = Modifier.padding(bottom = 4.dp))
        }
        if (!emptyState && s?.revenueChangePct != null) {
            Spacer(Modifier.height(6.dp))
            val positive = s.revenueChangePct >= 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (positive) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                    contentDescription = null,
                    tint = if (positive) c.success else c.danger,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${s.revenueChangePct.asSignedPct()} vs last period",
                    style = type.caption.copy(fontWeight = FontWeight.Bold),
                    color = if (positive) c.success else c.danger,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        HourlyBarChart(s?.hourlyRevenue ?: List(24) { 0.0 })
    }
}

@Composable
private fun HourlyBarChart(hourlyRevenue: List<Double>) {
    val c = Hungry.colors
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    // Business-hours window - matches the mockup's visible 11h..23h range rather than a full,
    // mostly-empty 24-bar day.
    val hours = (11..23).toList()
    val max = hours.mapNotNull { hourlyRevenue.getOrNull(it) }.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val neutralBar = c.outline
    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        val count = hours.size
        val gap = 4.dp.toPx()
        val barWidth = (size.width - gap * (count - 1)) / count
        hours.forEachIndexed { i, hour ->
            val v = hourlyRevenue.getOrNull(hour) ?: 0.0
            val x = i * (barWidth + gap)
            val isCurrent = hour == currentHour
            val isFuture = hour > currentHour
            if (isFuture) {
                drawRoundRect(
                    color = c.outline,
                    topLeft = Offset(x, size.height - 18.dp.toPx()),
                    size = Size(barWidth, 18.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))),
                )
            } else if (v > 0) {
                val barHeight = (v / max).toFloat() * size.height
                drawRoundRect(
                    color = if (isCurrent) c.primary else neutralBar,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                )
            }
        }
    }
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf(11, 14, 17, 20, 23).forEach { h ->
            Text("${h}h", style = Hungry.type.caption, color = c.inkMuted)
        }
    }
}

@Composable
private fun KpiGrid(s: StatsSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiTile("Orders", s.ordersCount.toString(), Modifier.weight(1f))
            KpiTile("Avg prep", s.avgPrepMinutes?.let { "${it.toInt()} min" } ?: "—", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiTile("Accepted", "${s.acceptedPct.toInt()}%", Modifier.weight(1f), Hungry.colors.success)
            KpiTile("Cancelled", s.cancelledCount.toString(), Modifier.weight(1f), if (s.cancelledCount > 0) Hungry.colors.danger else null)
        }
    }
}

@Composable
private fun KpiTile(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color? = null) {
    val c = Hungry.colors
    val type = Hungry.type
    Column(
        modifier
            .clip(HungryRadius.card)
            .background(c.surface)
            .padding(16.dp),
    ) {
        Text(label, style = type.label, color = c.inkMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, style = type.headline, color = valueColor ?: c.ink)
    }
}

@Composable
private fun TopItemsCard(s: StatsSummary) {
    val c = Hungry.colors
    val type = Hungry.type
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HungryRadius.card)
            .background(c.surface)
            .padding(16.dp),
    ) {
        Text("Top items", style = type.title, color = c.ink)
        Spacer(Modifier.height(10.dp))
        if (s.topItems.isEmpty()) {
            Text("No items sold in this period yet.", style = type.body, color = c.inkMuted)
        } else {
            val max = s.topItems.maxOf { it.quantity }.coerceAtLeast(1)
            s.topItems.forEachIndexed { index, item ->
                Column(Modifier.padding(top = if (index == 0) 0.dp else 14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name, style = type.bodyStrong, color = c.ink)
                        Text(item.quantity.toString(), style = type.bodyStrong, color = c.ink)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(HungryRadius.pill).background(c.surfaceSunken)) {
                        Box(
                            Modifier
                                .fillMaxWidth(item.quantity.toFloat() / max.toFloat())
                                .height(6.dp)
                                .clip(HungryRadius.pill)
                                .background(c.ink),
                        )
                    }
                }
            }
        }
    }
}
