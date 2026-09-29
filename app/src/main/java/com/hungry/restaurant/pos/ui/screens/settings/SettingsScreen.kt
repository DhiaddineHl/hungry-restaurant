package com.hungry.restaurant.pos.ui.screens.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.PrintDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.printer.SunmiPrinter
import com.hungry.restaurant.pos.ui.components.Avatar
import com.hungry.restaurant.pos.ui.components.ListCard
import com.hungry.restaurant.pos.ui.components.ListCardChevron
import com.hungry.restaurant.pos.ui.components.ListCardDivider
import com.hungry.restaurant.pos.ui.components.ListCardRow
import com.hungry.restaurant.pos.ui.components.SecondaryButton
import com.hungry.restaurant.pos.ui.components.SegmentedTabs
import com.hungry.restaurant.pos.ui.components.TextLink
import com.hungry.restaurant.pos.ui.components.Toggle
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius
import com.hungry.restaurant.pos.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onOpenAccount: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val c = Hungry.colors
    val type = Hungry.type
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val user by viewModel.authUser.collectAsStateWithLifecycle()
    val printerStatus by viewModel.printerStatus.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { onMessage(it) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.canvas),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text("Settings", style = type.headline, color = c.ink) }

        item {
            ProfileRow(name = user?.displayName ?: user?.email ?: "Account", subtitle = user?.email, onClick = onOpenAccount)
        }

        item { PrinterCard(printerStatus, onReconnect = viewModel::reconnectPrinter, onTestPrint = viewModel::printTestTicket) }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(HungryRadius.card)
                    .background(c.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Appearance", style = type.bodyStrong, color = c.ink)
                SegmentedTabs(
                    options = ThemeMode.entries,
                    selected = themeMode,
                    onSelect = viewModel::setThemeMode,
                    label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                    small = true,
                    modifier = Modifier.width(210.dp),
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TICKETS", style = type.overline, color = c.inkMuted)
                ListCard {
                    ToggleRow("Auto-print on accept", settings?.autoPrintOnAccept ?: true, viewModel::setAutoPrintOnAccept)
                    ListCardDivider()
                    StepperRow("Copies", settings?.ticketCopies ?: 1, min = 1, max = 5, onChange = viewModel::setTicketCopies)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ORDERS", style = type.overline, color = c.inkMuted)
                ListCard {
                    ToggleRow("Ring until accepted", settings?.ringUntilAccepted ?: true, viewModel::setRingUntilAccepted, caption = "Loud alert, max volume")
                    ListCardDivider()
                    StepperRow("Default prep time", settings?.defaultPrepTimeMinutes ?: 20, min = 1, max = 120, step = 5, suffix = " min", onChange = viewModel::setDefaultPrepTime)
                    ListCardDivider()
                    ToggleRow("Auto-accept orders", settings?.autoAcceptOrders ?: false, viewModel::setAutoAcceptOrders)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(name: String, subtitle: String?, onClick: () -> Unit) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(
        Modifier
            .fillMaxWidth()
            .clip(HungryRadius.card)
            .background(c.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = type.title, color = c.ink)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = type.caption, color = c.inkMuted)
            }
        }
        ListCardChevron()
    }
}

/**
 * §5 EmptyState-adjacent E9. The Sunmi printer library doesn't expose paper
 * sensor state or a print queue through this app's [SunmiPrinter] wrapper, so
 * this shows the printer's real connection status rather than a fabricated
 * "out of paper"/queued-tickets state the app has no way to detect.
 */
@Composable
private fun PrinterCard(status: SunmiPrinter.Status, onReconnect: () -> Unit, onTestPrint: () -> Unit) {
    val c = Hungry.colors
    val type = Hungry.type
    val connected = status == SunmiPrinter.Status.CONNECTED
    val statusLabel = when (status) {
        SunmiPrinter.Status.CONNECTED -> "Connected"
        SunmiPrinter.Status.CONNECTING -> "Connecting…"
        SunmiPrinter.Status.IDLE -> "Idle"
        SunmiPrinter.Status.UNAVAILABLE -> "Not available"
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HungryRadius.card)
            .background(c.surface)
            .then(if (!connected) Modifier.border(1.5.dp, c.danger, HungryRadius.card) else Modifier)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(if (connected) c.successSoft else c.dangerSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (connected) Icons.Outlined.Print else Icons.Outlined.PrintDisabled,
                    contentDescription = null,
                    tint = if (connected) c.success else c.danger,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Built-in printer", style = type.bodyStrong, color = c.ink)
                Text(statusLabel, style = type.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = if (connected) c.success else c.danger)
            }
            if (connected) {
                TextLink("Test print", onClick = onTestPrint)
            }
        }
        if (!connected) {
            Spacer(Modifier.height(12.dp))
            Text(
                "The printer isn't connected. Reconnect it to print tickets.",
                style = type.body,
                color = c.inkMuted,
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton("Check again", onClick = onReconnect, height = 44.dp)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit, caption: String? = null) {
    ListCardRow(title = label, caption = caption, trailing = { Toggle(checked = checked, onCheckedChange = onChange) })
}

@Composable
private fun StepperRow(label: String, value: Int, min: Int, max: Int, step: Int = 1, suffix: String = "", onChange: (Int) -> Unit) {
    val c = Hungry.colors
    val type = Hungry.type
    ListCardRow(
        title = label,
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CompactStepButton("−") { if (value - step >= min) onChange(value - step) }
                Text("$value$suffix", style = type.bodyStrong, color = c.ink, modifier = Modifier.padding(horizontal = 12.dp))
                CompactStepButton("+") { if (value + step <= max) onChange(value + step) }
            }
        },
    )
}

@Composable
private fun CompactStepButton(symbol: String, onClick: () -> Unit) {
    val c = Hungry.colors
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(c.surfaceSunken)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, style = Hungry.type.bodyStrong, color = c.ink)
    }
}
