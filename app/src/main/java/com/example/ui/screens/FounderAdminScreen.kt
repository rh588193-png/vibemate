package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminRole
import com.example.data.model.ModerationReport
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VibePost
import com.example.ui.components.VibeAvatar
import com.example.ui.components.VibeVerifiedBadge
import com.example.ui.components.formatRelativeTime
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibeEmerald
import com.example.ui.theme.VibePurple
import com.example.ui.theme.VibeRose

@Composable
fun FounderAdminScreen(
    adminRole: AdminRole?,
    postsState: UiState<List<VibePost>>,
    usersState: UiState<List<UserProfile>>,
    reportsState: UiState<List<ModerationReport>>,
    isBusy: Boolean,
    onBack: () -> Unit,
    onResolveReport: (reportId: String, targetPostId: String, newStatus: String, removePost: Boolean, note: String) -> Unit,
    onToggleUserVerified: (userId: String, verified: Boolean) -> Unit
) {
    BackHandler { onBack() }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 880.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("founder_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.Shield, contentDescription = null, tint = VibeElectricBlue)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Founder Admin Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Platform Owner: Raja • Server-Verified RBAC",
                        style = MaterialTheme.typography.labelSmall.copy(color = VibeElectricBlue)
                    )
                }
            }

            if (adminRole == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, VibeRose.copy(alpha = 0.5f))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = VibeRose, modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Server Authorization Required", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Access to the Founder Console is strictly enforced by Cloud Firestore security rules for verified platform administrators.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                val posts = (postsState as? UiState.Success)?.data ?: emptyList()
                val users = (usersState as? UiState.Success)?.data ?: emptyList()
                val reports = (reportsState as? UiState.Success)?.data ?: emptyList()
                val openReports = reports.filter { it.status == "open" }
                val reelsCount = posts.count { it.postType == "reel" }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // KPI Metrics Grid
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AdminStatCard("Creators", "${users.size}", VibeElectricBlue, Modifier.weight(1f))
                            AdminStatCard("Posts", "${posts.size}", VibePurple, Modifier.weight(1f))
                            AdminStatCard("Reels", "$reelsCount", VibeEmerald, Modifier.weight(1f))
                            AdminStatCard("Open Flags", "${openReports.size}", VibeRose, Modifier.weight(1f))
                        }
                    }

                    // Moderation Queue Section
                    item {
                        Text(
                            text = "Moderation Queue (${reports.size})",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    when (reportsState) {
                        is UiState.Loading -> item {
                            CircularProgressIndicator(color = VibeElectricBlue)
                        }
                        is UiState.Error -> item {
                            Text(reportsState.message, color = MaterialTheme.colorScheme.error)
                        }
                        is UiState.Success -> {
                            if (reports.isEmpty()) {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "No flagged content reports in the moderation queue.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            } else {
                                items(reports, key = { it.id }) { report ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(18.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Post #${report.targetPostId.take(8)}",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Surface(
                                                    color = when (report.status) {
                                                        "open" -> VibeRose.copy(alpha = 0.2f)
                                                        "resolved" -> VibeEmerald.copy(alpha = 0.2f)
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    },
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text(
                                                        text = report.status.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = "Reason: ${report.reason}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Reported ${formatRelativeTime(report.createdAt)}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                            if (report.status == "open") {
                                                Spacer(Modifier.height(10.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = {
                                                            onResolveReport(
                                                                report.id,
                                                                report.targetPostId,
                                                                "resolved",
                                                                true,
                                                                "Removed by Founder Moderation"
                                                            )
                                                        },
                                                        enabled = !isBusy,
                                                        colors = ButtonDefaults.buttonColors(containerColor = VibeRose)
                                                    ) {
                                                        Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Remove Post")
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            onResolveReport(
                                                                report.id,
                                                                report.targetPostId,
                                                                "dismissed",
                                                                false,
                                                                "Reviewed and cleared"
                                                            )
                                                        },
                                                        enabled = !isBusy
                                                    ) {
                                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Dismiss")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Creator Verification Management
                    item {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Creator Verification & Directory (${users.size})",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(users, key = { it.userId }) { user ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                VibeAvatar(
                                    displayName = user.displayName,
                                    preset = user.avatarPreset,
                                    size = 40.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.displayName,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (user.founderBadge || user.verified) {
                                            Spacer(Modifier.width(6.dp))
                                            VibeVerifiedBadge(founder = user.founderBadge)
                                        }
                                    }
                                    Text(
                                        text = "@${user.handle} • ${user.followersCount} followers • ${user.postsCount} posts",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Verified status",
                                        tint = if (user.verified) VibeElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Switch(
                                        checked = user.verified,
                                        onCheckedChange = { checked ->
                                            onToggleUserVerified(user.userId, checked)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
