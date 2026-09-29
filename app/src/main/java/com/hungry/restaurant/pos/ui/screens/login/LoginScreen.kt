package com.hungry.restaurant.pos.ui.screens.login

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.BuildConfig
import com.hungry.restaurant.pos.ui.components.Avatar
import com.hungry.restaurant.pos.ui.components.Banner
import com.hungry.restaurant.pos.ui.components.ListCard
import com.hungry.restaurant.pos.ui.components.PrimaryButton
import com.hungry.restaurant.pos.ui.components.SecondaryButton
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val c = Hungry.colors
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Launches Keycloak's authorization page in a Chrome Custom Tab (AppAuth's default —
    // no WebView involved) and delivers the redirect back to the ViewModel.
    val authLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result -> viewModel.onAuthorizationResult(result) }

    fun launchSignIn() {
        scope.launch {
            try {
                authLauncher.launch(viewModel.buildAuthorizationIntent())
            } catch (e: Throwable) {
                // Throwable, not Exception: a missing/broken Custom Tabs or WebView
                // provider on this device can surface as an Error subtype (e.g.
                // NoClassDefFoundError), which would otherwise slip past this catch
                // and crash the app.
                Log.e("LoginScreen", "Could not start sign-in", e)
                Toast.makeText(context, e.message ?: "Could not start sign-in", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(state) {
        if (state is LoginUiState.Authenticated) onLoginSuccess()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.canvas)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        BrandHeader()
        Spacer(Modifier.height(40.dp))

        Column(Modifier.weight(1f).fillMaxWidth()) {
            when (val current = state) {
                is LoginUiState.SignedOut -> SignInBody(onSignIn = ::launchSignIn)
                is LoginUiState.Error -> SignInBody(errorMessage = current.message, onSignIn = ::launchSignIn)
                is LoginUiState.Authenticating -> AuthenticatingBody(current)
                is LoginUiState.Authenticated -> AuthenticatingBody(LoginUiState.Authenticating(AuthStep.LOADING_ORDERS))
                is LoginUiState.AccessDenied -> {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                        Spacer(Modifier.weight(1f))
                        AccessDeniedBody(current, onUseAnotherAccount = viewModel::retry)
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    val c = Hungry.colors
    val type = Hungry.type
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            buildAnnotatedString {
                append("hungry")
                withStyle(SpanStyle(color = c.primary)) { append(".") }
            },
            style = type.title,
            color = c.ink,
        )
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .clip(HungryRadius.pill)
                .background(c.inverseSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("PARTNER", style = type.badge, color = c.onInverseSurface)
        }
    }
}

/** Screen 01/E1b - headline + optional error banner up top, sign-in controls pinned to the bottom. */
@Composable
private fun SignInBody(onSignIn: () -> Unit, errorMessage: String? = null) {
    val c = Hungry.colors
    val type = Hungry.type
    Column(Modifier.fillMaxSize()) {
        Text(
            buildAnnotatedString {
                append("Sign in to start taking orders")
                withStyle(SpanStyle(color = c.primary)) { append(".") }
            },
            style = type.hero,
            color = c.ink,
        )
        Spacer(Modifier.height(10.dp))
        Text("Use the Hungry account linked to your restaurant.", style = type.body, color = c.inkMuted)

        if (errorMessage != null) {
            Spacer(Modifier.height(20.dp))
            Banner(
                icon = Icons.Outlined.WifiOff,
                title = "Couldn't reach Hungry",
                body = errorMessage,
                fg = c.danger,
                bg = c.dangerSoft,
            )
        }

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            label = if (errorMessage != null) "Try again" else "Sign in with Hungry",
            onClick = onSignIn,
            trailingIcon = Icons.Outlined.OpenInNew,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lock, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Opens Hungry's secure sign-in page", style = type.caption, color = c.inkMuted)
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = c.outline, thickness = 1.dp)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row {
                Text("Need access? ", style = type.caption, color = c.inkMuted)
                Text(
                    "Get help",
                    style = type.caption.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
                    color = c.ink,
                )
            }
            Text("v${BuildConfig.VERSION_NAME} · ${android.os.Build.MODEL}", style = type.caption, color = c.inkMuted)
        }
        Spacer(Modifier.height(20.dp))
    }
}

/** Screen 01b - the 3-step sign-in reveal. */
@Composable
private fun AuthenticatingBody(state: LoginUiState.Authenticating) {
    val c = Hungry.colors
    val type = Hungry.type
    Column {
        Text(
            buildAnnotatedString {
                append("Signing you in")
                withStyle(SpanStyle(color = c.primary)) { append("…") }
            },
            style = type.hero,
            color = c.ink,
        )
        Spacer(Modifier.height(10.dp))
        Text("This takes a few seconds.", style = type.body, color = c.inkMuted)
        Spacer(Modifier.height(24.dp))
        ListCard {
            StepRow(label = "Account verified", done = true, active = state.step == AuthStep.ACCOUNT_VERIFIED)
            StepRow(
                label = "Connecting to ${state.restaurantName ?: "Hungry"}",
                done = state.step.ordinal > AuthStep.CONNECTING.ordinal,
                active = state.step == AuthStep.CONNECTING,
            )
            StepRow(label = "Loading live orders", done = false, active = state.step == AuthStep.LOADING_ORDERS)
        }
    }
}

@Composable
private fun StepRow(label: String, done: Boolean, active: Boolean) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            when {
                done -> Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = c.success, modifier = Modifier.size(22.dp))
                active -> CircularProgressIndicator(modifier = Modifier.size(18.dp), color = c.primary, strokeWidth = 2.dp)
                else -> Box(Modifier.size(10.dp).clip(CircleShape).background(c.outline))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(label, style = type.bodyStrong, color = if (done || active) c.ink else c.inkMuted)
    }
}

/** Screen E1 - no restaurant linked to this Keycloak account. */
@Composable
private fun AccessDeniedBody(state: LoginUiState.AccessDenied, onUseAnotherAccount: () -> Unit) {
    val c = Hungry.colors
    val type = Hungry.type
    Column {
        Box(Modifier.size(72.dp).clip(CircleShape).background(c.dangerSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Block, contentDescription = null, tint = c.danger, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("This account can't open the POS", style = type.headline, color = c.ink)
        if (state.email != null) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.clip(HungryRadius.pill).background(c.surface).padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(state.email, size = 28.dp, isCurrentUser = false)
                Spacer(Modifier.width(8.dp))
                Text(state.email, style = type.bodyStrong, color = c.ink)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "It isn't linked to a restaurant yet. Ask your Hungry account manager to add restaurant access, or sign in with another account.",
            style = type.body,
            color = c.inkMuted,
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Use another account", onClick = onUseAnotherAccount)
        Spacer(Modifier.height(10.dp))
        SecondaryButton("Contact partner support", onClick = {}, leadingIcon = Icons.Outlined.HelpOutline)
    }
}
