package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminRole
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VibePost
import com.example.ui.components.VibeAvatar
import com.example.ui.components.VibeMediaSurface
import com.example.ui.components.VibeVerifiedBadge
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibePurple

@Composable
fun ProfileScreen(
    currentUserId: String,
    profile: UserProfile?,
    adminRole: AdminRole?,
    postsState: UiState<List<VibePost>>,
    bookmarkedPostIds: Set<String>,
    isBusy: Boolean,
    onUpdateProfile: (displayName: String, handle: String, bio: String, avatarPreset: String, onDone: () -> Unit) -> Unit,
    onClaimFounderAccess: () -> Unit,
    onOpenFounderAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = My Posts, 1 = Saved
    var showEditDialog by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (profile == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VibeElectricBlue)
            }
        } else {
            val allPosts = (postsState as? UiState.Success)?.data ?: emptyList()
            val myPosts = allPosts.filter { it.userId == currentUserId }
            val savedPosts = allPosts.filter { bookmarkedPostIds.contains(it.id) }
            val shownPosts = if (selectedTab == 0) myPosts else savedPosts

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Column {
                        // Top bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "@${profile.handle}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign out",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Avatar + Stats Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            VibeAvatar(
                                displayName = profile.displayName,
                                preset = profile.avatarPreset,
                                size = 78.dp,
                                showRing = true
                            )
                            Spacer(Modifier.width(20.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.weight(1f)
                            ) {
                                ProfileMetric("${myPosts.size}", "Posts")
                                ProfileMetric("${profile.followersCount}", "Followers")
                                ProfileMetric("${profile.followingCount}", "Following")
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (profile.founderBadge || adminRole != null) {
                                Spacer(Modifier.width(8.dp))
                                VibeVerifiedBadge(founder = true)
                            } else if (profile.verified) {
                                Spacer(Modifier.width(6.dp))
                                VibeVerifiedBadge(founder = false)
                            }
                        }

                        if (profile.bio.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = profile.bio,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { showEditDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_profile_button")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Edit Profile")
                            }

                            if (adminRole != null) {
                                Button(
                                    onClick = onOpenFounderAdmin,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_founder_dashboard_button")
                                ) {
                                    Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Founder Console")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onClaimFounderAccess,
                                    enabled = !isBusy,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("verify_founder_button")
                                ) {
                                    Icon(Icons.Filled.Shield, contentDescription = null, tint = VibeElectricBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Founder Access")
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Filled.GridOn, contentDescription = "Posts") },
                                text = { Text("Posts") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Filled.Bookmark, contentDescription = "Saved") },
                                text = { Text("Saved") }
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (shownPosts.isEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = if (selectedTab == 0) "No posts published yet." else "No saved bookmarks yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(shownPosts, key = { it.id }) { post ->
                        VibeMediaSurface(
                            mediaKey = post.mediaKey,
                            mediaType = post.mediaType,
                            contentDescription = post.caption.ifBlank { "Profile post" },
                            aspectRatio = 1f
                        )
                    }
                }
            }

            if (showEditDialog) {
                EditProfileDialog(
                    profile = profile,
                    isBusy = isBusy,
                    onDismiss = { showEditDialog = false },
                    onSave = { name, handle, bio, preset ->
                        onUpdateProfile(name, handle, bio, preset) {
                            showEditDialog = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, handle: String, bio: String, preset: String) -> Unit
) {
    var name by remember { mutableStateOf(profile.displayName) }
    var handle by remember { mutableStateOf(profile.handle) }
    var bio by remember { mutableStateOf(profile.bio) }
    var preset by remember { mutableStateOf(profile.avatarPreset) }

    val presets = listOf(
        "prism_violet" to "Violet",
        "electric_cyan" to "Electric",
        "obsidian_neon" to "Obsidian",
        "rose_prism" to "Rose"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit VIBEMATE Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 50) name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = handle,
                    onValueChange = {
                        val clean = it.lowercase().replace(Regex("[^a-z0-9_.]"), "")
                        if (clean.length <= 32) handle = clean
                    },
                    label = { Text("Handle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 200) bio = it },
                    label = { Text("Bio") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Avatar Prism Theme", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEach { (key, label) ->
                        val active = preset == key
                        Surface(
                            onClick = { preset = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) VibePurple else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (active) VibeElectricBlue else MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, handle, bio, preset) },
                enabled = name.isNotBlank() && handle.length >= 2 && !isBusy
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
