package com.hungry.restaurant.pos.ui.screens.staffpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.data.model.StaffMember
import com.hungry.restaurant.pos.ui.theme.HungryOrange

@Composable
fun StaffPickerScreen(
    onSignedIn: () -> Unit,
    onManagerLogin: () -> Unit,
    viewModel: StaffPickerViewModel = viewModel(factory = StaffPickerViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddStaff by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.signedIn.collect { onSignedIn() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Text(
            state.restaurant?.name ?: "Hungry Partner",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Who's on shift?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(20.dp))

        if (state.staff.isEmpty()) {
            if (!state.loading) {
                EmptyStaffState(onAddStaff = { showAddStaff = true })
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(state.staff, key = { it.id }) { staff ->
                    StaffAvatar(
                        staff = staff,
                        selected = staff.id == state.selectedStaffId,
                        onClick = { viewModel.selectStaff(staff.id) },
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            PinDots(pin = state.pin, error = state.pinError)

            Spacer(Modifier.height(28.dp))

            Keypad(
                onDigit = viewModel::onDigit,
                onBackspace = viewModel::onBackspace,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            )

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = { showAddStaff = true }) {
                    Text("Add staff member")
                }
                TextButton(onClick = onManagerLogin) {
                    Text("Manager? Sign in again")
                }
            }
        }
    }

    if (showAddStaff) {
        AddStaffDialog(
            onDismiss = { showAddStaff = false },
            onConfirm = { name, pin ->
                viewModel.addStaffMember(name, pin) { created ->
                    showAddStaff = false
                    if (created != null) viewModel.selectStaff(created.id)
                }
            },
        )
    }
}

@Composable
private fun EmptyStaffState(onAddStaff: () -> Unit) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            "No staff set up yet.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddStaff, shape = RoundedCornerShape(14.dp)) {
            androidx.compose.material3.Icon(Icons.Outlined.PersonAddAlt, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add your first team member")
        }
    }
}

@Composable
private fun StaffAvatar(staff: StaffMember, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (selected) HungryOrange else MaterialTheme.colorScheme.surfaceVariant)
                .then(Modifier.padding(2.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                staff.initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            staff.name.substringBefore(" "),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PinDots(pin: String, error: Boolean) {
    val color = if (error) MaterialTheme.colorScheme.error else HungryOrange
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(PIN_LENGTH) { index ->
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (index < pin.length) color else MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, modifier: Modifier = Modifier) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { digit ->
                    KeypadKey(label = digit.toString(), modifier = Modifier.weight(1f)) { onDigit(digit) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f))
            KeypadKey(label = "0", modifier = Modifier.weight(1f)) { onDigit('0') }
            KeypadKey(icon = Icons.AutoMirrored.Outlined.Backspace, modifier = Modifier.weight(1f), onClick = onBackspace)
        }
    }
}

@Composable
private fun KeypadKey(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        } else if (icon != null) {
            androidx.compose.material3.Icon(icon, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun AddStaffDialog(onDismiss: () -> Unit, onConfirm: (name: String, pin: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a staff member") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= PIN_LENGTH && it.all(Char::isDigit)) pin = it },
                    label = { Text("4-digit PIN") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, pin) },
                enabled = name.isNotBlank() && pin.length == PIN_LENGTH,
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
