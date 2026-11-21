package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.auth.attemptAutoSignIn
import com.example.data.auth.onGoogleSignInClicked
import com.example.ui.components.VibePrimaryGradientButton
import com.example.ui.components.VibePrismMark
import com.example.ui.theme.VibeCarbon
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibeGradients
import com.example.ui.theme.VibeMutedSlate
import com.example.ui.theme.VibeObsidian
import com.example.ui.theme.VibePurple
import com.example.ui.theme.VibeRose
import com.example.ui.theme.VibeSurfaceBorder
import com.example.ui.theme.VibeWhite

@Composable
fun AuthScreen(
    onAuthSuccess: (handle: String, displayName: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Create Account
    var displayNameInput by remember { mutableStateOf("") }
    var handleInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { onAuthSuccess(handleInput, displayNameInput) },
            onUnauthenticated = { },
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeObsidian)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_vibe_splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.28f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 460.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_vibemate_logo),
                        contentDescription = "VIBEMATE emblem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, VibeGradients.PrismBrand, RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge.copy(color = VibeWhite)
                            )
                            Spacer(Modifier.width(6.dp))
                            VibePrismMark(size = 20.dp)
                        }
                        Text(
                            text = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.labelLarge.copy(color = VibeElectricBlue)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Surface(
                    color = VibeCarbon.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, VibeSurfaceBorder),
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = VibeObsidian,
                            contentColor = VibeWhite,
                            modifier = Modifier.clip(RoundedCornerShape(14.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    errorMessage = null
                                },
                                text = {
                                    Text(
                                        "Sign In",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selectedTab == 0) VibeElectricBlue else VibeMutedSlate
                                    )
                                },
                                modifier = Modifier.testTag("auth_tab_signin")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    errorMessage = null
                                },
                                text = {
                                    Text(
                                        "Create Account",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selectedTab == 1) VibeElectricBlue else VibeMutedSlate
                                    )
                                },
                                modifier = Modifier.testTag("auth_tab_register")
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        if (selectedTab == 0) {
                            Text(
                                text = "Welcome back to the prism",
                                style = MaterialTheme.typography.headlineMedium.copy(color = VibeWhite)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Sign in securely with your Google account to sync your feed, reels, followers, and notifications.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = VibeMutedSlate)
                            )
                        } else {
                            Text(
                                text = "Claim your creator identity",
                                style = MaterialTheme.typography.headlineMedium.copy(color = VibeWhite)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Pick your VIBEMATE handle and display name, then verify with Google Identity to activate your profile.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = VibeMutedSlate)
                            )
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = displayNameInput,
                                onValueChange = { if (it.length <= 50) displayNameInput = it },
                                label = { Text("Display Name") },
                                placeholder = { Text("e.g. Raja") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = VibeElectricBlue)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VibeElectricBlue,
                                    unfocusedBorderColor = VibeSurfaceBorder,
                                    focusedTextColor = VibeWhite,
                                    unfocusedTextColor = VibeWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_name_input")
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = handleInput,
                                onValueChange = {
                                    val filtered = it.lowercase().replace(Regex("[^a-z0-9_.]"), "")
                                    if (filtered.length <= 32) handleInput = filtered
                                },
                                label = { Text("Creator Handle") },
                                placeholder = { Text("e.g. raja_official") },
                                leadingIcon = {
                                    Icon(Icons.Filled.AlternateEmail, contentDescription = null, tint = VibePurple)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VibePurple,
                                    unfocusedBorderColor = VibeSurfaceBorder,
                                    focusedTextColor = VibeWhite,
                                    unfocusedTextColor = VibeWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_handle_input")
                            )
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Surface(
                                color = VibeRose.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, VibeRose.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall.copy(color = VibeRose),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        VibePrimaryGradientButton(
                            text = if (selectedTab == 0) "Sign in with Google" else "Create Account with Google",
                            loading = isLoading,
                            testTag = "google_signin_button",
                            onClick = {
                                if (selectedTab == 1 && handleInput.trim().isNotEmpty() && handleInput.trim().length < 2) {
                                    errorMessage = "Creator handle must be at least 2 characters."
                                    return@VibePrimaryGradientButton
                                }
                                errorMessage = null
                                isLoading = true
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess(handleInput, displayNameInput)
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isLoading = false
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = VibeMutedSlate,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Secured by Firebase Auth & Credential Manager",
                                style = MaterialTheme.typography.bodySmall.copy(color = VibeMutedSlate),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "VIBEMATE Global Social Platform • Founded by Raja",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = VibeMutedSlate,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
