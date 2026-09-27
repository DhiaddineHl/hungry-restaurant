package com.hungry.restaurant.pos.ui.screens.stats

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
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.hungry.restaurant.pos.data.model.StatsPeriod
import com.hungry.restaurant.pos.data.model.StatsSummary
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.data.model.asSignedPct
import com.hungry.restaurant.pos.ui.theme.HungryOrange
import com.hungry.restaurant.pos.ui.theme.HungryOrangeDark
import com.hungry.restaurant.pos.ui.theme.NegativeRed
import com.hungry.restaurant.pos.ui.theme.PositiveGreen

@Composable
fun StatsScreen(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory),
) {
    val period by viewModel.period.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

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
            Text("Stats", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatsPeriod.entries.forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { viewModel.selectPeriod(p) },
                        label = { Text(p.name.lowercase().replaceFirstChar(Char::uppercase)) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        }

        val s = stats ?: return@LazyColumn
        item { RevenueHero(s) }
        item { KpiGrid(s) }
        item { HourlyRevenueCard(s) }
        item { TopItemsCard(s) }
    }
}

@Composable
private fun RevenueHero(s: StatsSummary) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(HungryOrange, HungryOrangeDark)))
            .padding(20.dp),
    ) {
        Text("Revenue", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
        Spacer(Modifier.height(6.dp))
        Text(
            s.revenue.asCurrency(s.currency),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        s.revenueChangePct?.let { delta ->
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (delta >= 0) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("${delta.asSignedPct()} vs previous period", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }
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
            KpiTile("Accepted", "${s.acceptedPct.toInt()}%", Modifier.weight(1f), PositiveGreen)
            KpiTile("Cancelled", s.cancelledCount.toString(), Modifier.weight(1f), if (s.cancelledCount > 0) NegativeRed else null)
        }
    }
}

@Composable
private fun KpiTile(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color? = null) {
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
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HourlyRevenueCard(s: StatsSummary) {
    SectionCard(title = "Revenue by hour") {
        val max = (s.hourlyRevenue.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(top = 8.dp),
        ) {
            val count = s.hourlyRevenue.size
            if (count == 0) return@Canvas
            val gap = 2.dp.toPx()
            val barWidth = (size.width - gap * (count - 1)) / count
            s.hourlyRevenue.forEachIndexed { i, v ->
                val barHeight = (v / max).toFloat() * size.height
                val x = i * (barWidth + gap)
                drawRoundRect(
                    color = HungryOrange.copy(alpha = 0.18f),
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(3f, 3f),
                )
                if (barHeight > 0f) {
                    drawRoundRect(
                        color = HungryOrange,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3f, 3f),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("12am", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("12pm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("11pm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TopItemsCard(s: StatsSummary) {
    SectionCard(title = "Top selling items") {
        if (s.topItems.isEmpty()) {
            Text(
                "No items sold in this period yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            return@SectionCard
        }
        Column(modifier = Modifier.padding(top = 4.dp)) {
            s.topItems.forEachIndexed { index, item ->
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
                    Text("×${item.quantity}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        content()
    }
}
