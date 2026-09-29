package com.hungry.restaurant.pos.ui.screens.account

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.ui.components.Avatar
import com.hungry.restaurant.pos.ui.components.HungryTopBar
import com.hungry.restaurant.pos.ui.components.ListCard
import com.hungry.restaurant.pos.ui.components.ListCardChevron
import com.hungry.restaurant.pos.ui.components.ListCardDivider
import com.hungry.restaurant.pos.ui.components.ListCardRow
import com.hungry.restaurant.pos.ui.components.SecondaryButton
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory),
) {
    val c = Hungry.colors
    val type = Hungry.type
    val user by viewModel.authUser.collectAsStateWithLifecycle()
    val restaurant by viewModel.restaurant.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // The end-session redirect lands back here the same way the sign-in one does; either
    // way it resolves, drop the local session so stale tokens aren't reused.
    val logoutLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.onLogoutResult()
        onLoggedOut()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.canvas)
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding() + 24.dp),
    ) {
        Spacer(Modifier.height(contentPadding.calculateTopPadding()))
        HungryTopBar(title = "Account", onBack = onBack)

        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            val current = user
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(current?.displayName ?: current?.email ?: "?", size = 64.dp)
                Spacer(Modifier.height(12.dp))
                Text(current?.displayName ?: current?.email ?: "Signed in", style = type.title, color = c.ink)
                val email = current?.email
                if (email != null && email != current.displayName) {
                    Text(email, style = type.body, color = c.inkMuted)
                }
            }

            restaurant?.let { profile ->
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(HungryRadius.card)
                        .background(c.surface)
                        .padding(16.dp),
                ) {
                    Column {
                        Text(profile.name, style = type.bodyStrong, color = c.ink)
                        Text(
                            if (profile.acceptingOrders) "Open for orders · Terminal ${android.os.Build.MODEL}" else "Closed · Terminal ${android.os.Build.MODEL}",
                            style = type.caption,
                            color = c.inkMuted,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ListCard {
                ListCardRow(
                    title = "Manage account",
                    onClick = { context.startActivity(viewModel.buildManageAccountIntent()) },
                    trailing = { ListCardChevron() },
                )
                ListCardDivider()
                ListCardRow(
                    title = "Hungry partner support",
                    onClick = { /* no support channel wired up yet */ },
                    trailing = { ListCardChevron() },
                )
            }

            Spacer(Modifier.height(28.dp))

            SecondaryButton(
                "Sign out",
                onClick = { scope.launch { logoutLauncher.launch(viewModel.buildLogoutIntent()) } },
                destructive = true,
                leadingIcon = Icons.AutoMirrored.Outlined.Logout,
            )
        }
    }
}
