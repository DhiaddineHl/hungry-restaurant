package com.hungry.restaurant.pos.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.printer.SunmiPrinter
import com.hungry.restaurant.pos.ui.theme.HungryOrange

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onOpenAccount: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val user by viewModel.authUser.collectAsStateWithLifecycle()
    val printerStatus by viewModel.printerStatus.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { onMessage(it) }
    }

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
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }

        item {
            ProfileRow(name = user?.displayName ?: user?.email ?: "Account", role = "", onClick = onOpenAccount)
        }

        item {
            SectionCard(title = "PRINTER") {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            when (printerStatus) {
                                SunmiPrinter.Status.CONNECTED -> "Connected"
                                SunmiPrinter.Status.CONNECTING -> "Connecting…"
                                SunmiPrinter.Status.IDLE -> "Idle"
                                SunmiPrinter.Status.UNAVAILABLE -> "Not available"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text("Built-in thermal printer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (printerStatus != SunmiPrinter.Status.CONNECTED) {
                        OutlinedButton(onClick = viewModel::reconnectPrinter) { Text("Reconnect") }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = viewModel::printTestTicket,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Print test ticket")
                }
            }
        }

        item {
            SectionCard(title = "TICKETS") {
                SwitchRow("Auto-print on accept", settings?.autoPrintOnAccept ?: true, viewModel::setAutoPrintOnAccept)
                StepperRow("Copies", settings?.ticketCopies ?: 1, min = 1, max = 5, onChange = viewModel::setTicketCopies)
            }
        }

        item {
            SectionCard(title = "ORDERS") {
                SwitchRow("Ring until accepted", settings?.ringUntilAccepted ?: true, viewModel::setRingUntilAccepted)
                StepperRow("Default prep time (min)", settings?.defaultPrepTimeMinutes ?: 20, min = 1, max = 120, step = 5, onChange = viewModel::setDefaultPrepTime)
                SwitchRow("Auto-accept orders", settings?.autoAcceptOrders ?: false, viewModel::setAutoAcceptOrders)
            }
        }
    }
}

@Composable
private fun ProfileRow(name: String, role: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val initial = name.take(1).uppercase()
        androidx.compose.foundation.layout.Box(
            Modifier.size(44.dp).clip(CircleShape).background(HungryOrange),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (role.isNotBlank()) {
                Text(role.lowercase().replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun StepperRow(label: String, value: Int, min: Int, max: Int, step: Int = 1, onChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { if (value - step >= min) onChange(value - step) }) { Text("−") }
            Text("$value", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
            OutlinedButton(onClick = { if (value + step <= max) onChange(value + step) }) { Text("+") }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        content()
    }
}
