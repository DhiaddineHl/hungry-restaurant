package com.hungry.restaurant.pos.ui.screens.metrics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.data.model.DashboardMetrics
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.data.model.asSignedPct
import com.hungry.restaurant.pos.ui.theme.HungryOrange
import com.hungry.restaurant.pos.ui.theme.HungryOrangeDark
import com.hungry.restaurant.pos.ui.theme.NegativeRed
import com.hungry.restaurant.pos.ui.theme.PositiveGreen

@Composable
fun MetricsScreen(
    contentPadding: PaddingValues,
    viewModel: MetricsViewModel = viewModel(factory = MetricsViewModel.Factory),
) {
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "Metrics Overview",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        val m = metrics ?: return@LazyColumn
        item { RevenueHero(m) }
        item { KpiGrid(m) }
        item { RevenueTrendCard(m) }
        item { PlatformBreakdownCard(m) }
        item { TopItemsCard(m) }
    }
}

@Composable
private fun RevenueHero(m: DashboardMetrics) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(HungryOrange, HungryOrangeDark)))
            .padding(20.dp),
    ) {
        Text(
            "Revenue today",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            m.revenueTodayCents.asCurrency(),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Outlined.TrendingUp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${m.revenueChangePct.asSignedPct()} vs yesterday",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun KpiGrid(m: DashboardMetrics) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiTile("Orders today", m.ordersToday.toString(), m.ordersChangePct.asSignedPct(), true, Modifier.weight(1f))
            KpiTile("Avg prep", "${m.avgPrepMinutes} min", null, true, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiTile("Active", m.activeOrders.toString(), null, true, Modifier.weight(1f))
            KpiTile("Completed", m.completedToday.toString(), null, true, Modifier.weight(1f))
            KpiTile("Cancelled", m.cancelledToday.toString(), null, false, Modifier.weight(1f))
        }
    }
}

@Composable
private fun KpiTile(
    label: String,
    value: String,
    delta: String?,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (delta != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                delta,
                style = MaterialTheme.typography.labelSmall,
                color = if (positive) PositiveGreen else NegativeRed,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RevenueTrendCard(m: DashboardMetrics) {
    SectionCard(title = "Revenue · last 7 days") {
        RevenueBarChart(
            values = m.revenueTrendCents,
            labels = m.trendLabels,
            barColor = HungryOrange,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun RevenueBarChart(
    values: List<Int>,
    labels: List<String>,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    val max = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            val count = values.size
            if (count == 0) return@Canvas
            val gap = 12.dp.toPx()
            val barWidth = (size.width - gap * (count - 1)) / count
            values.forEachIndexed { i, v ->
                val barHeight = (v.toFloat() / max) * size.height
                val x = i * (barWidth + gap)
                drawRoundRect(
                    color = barColor.copy(alpha = 0.18f),
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(10f, 10f),
                )
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(10f, 10f),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = labelColor)
            }
        }
    }
}

@Composable
private fun PlatformBreakdownCard(m: DashboardMetrics) {
    val totalOrders = m.platformBreakdown.sumOf { it.orders }
    SectionCard(title = "Orders by platform") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            m.platformBreakdown.forEach { share ->
                val brand = Color(share.platform.brandHex)
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            share.platform.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "${share.orders} · ${share.revenueCents.asCurrency()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (share.sharePct(totalOrders) / 100.0).toFloat() },
                        color = brand,
                        trackColor = brand.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopItemsCard(m: DashboardMetrics) {
    SectionCard(title = "Top selling items") {
        Column(modifier = Modifier.padding(top = 4.dp)) {
            m.topItems.forEachIndexed { index, item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "×${item.soldCount}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}
