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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VibePost
import com.example.ui.components.VibeAvatar
import com.example.ui.components.VibePostCard
import com.example.ui.components.VibePrimaryGradientButton
import com.example.ui.components.VibePrismMark
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibePurple

@Composable
fun HomeFeedScreen(
    currentUserId: String,
    currentUserProfile: UserProfile?,
    postsState: UiState<List<VibePost>>,
    usersState: UiState<List<UserProfile>>,
    likedPostIds: Set<String>,
    bookmarkedPostIds: Set<String>,
    followingIds: Set<String>,
    isAdmin: Boolean,
    isDarkTheme: Boolean,
    isBusy: Boolean,
    onToggleTheme: () -> Unit,
    onOpenFounderAdmin: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onSyncLikes: (List<VibePost>) -> Unit,
    onLikePost: (VibePost) -> Unit,
    onCommentPost: (VibePost) -> Unit,
    onBookmarkPost: (VibePost) -> Unit,
    onSharePost: (VibePost) -> Unit,
    onFollowAuthor: (VibePost) -> Unit,
    onReportPost: (VibePost) -> Unit,
    onSeedShowcase: () -> Unit
) {
    var filterChip by remember { mutableStateOf("For You") }

    LaunchedEffect(postsState) {
        if (postsState is UiState.Success) {
            onSyncLikes(postsState.data)
        }
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            // Top Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                VibePrismMark(size = 30.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VIBEMATE",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Create. Share. Connect.",
                        style = MaterialTheme.typography.labelSmall.copy(color = VibeElectricBlue)
                    )
                }

                if (isAdmin) {
                    Surface(
                        onClick = onOpenFounderAdmin,
                        shape = RoundedCornerShape(14.dp),
                        color = VibePurple.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, VibeElectricBlue.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("home_founder_shortcut")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "Founder Console",
                                tint = VibeElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Founder",
                                style = MaterialTheme.typography.labelMedium.copy(color = VibeElectricBlue)
                            )
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Toggle theme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Active Creators / Vibes Strip
                item {
                    val creators = (usersState as? UiState.Success)?.data ?: emptyList()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(68.dp)
                            ) {
                                Surface(
                                    onClick = onNavigateToCreate,
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.5.dp, VibeElectricBlue),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.AddCircleOutline,
                                            contentDescription = "New vibe",
                                            tint = VibeElectricBlue
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Your Vibe",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        items(creators, key = { it.userId }) { creator ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(68.dp)
                            ) {
                                VibeAvatar(
                                    displayName = creator.displayName,
                                    preset = creator.avatarPreset,
                                    size = 50.dp,
                                    showRing = true
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "@${creator.handle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Feed Filter Pills
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("For You", "Following", "Photos", "Videos").forEach { chip ->
                            val selected = filterChip == chip
                            Surface(
                                onClick = { filterChip = chip },
                                shape = RoundedCornerShape(18.dp),
                                color = if (selected) VibePurple else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) VibeElectricBlue else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (selected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                when (postsState) {
                    is UiState.Loading -> {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            ) {
                                CircularProgressIndicator(color = VibeElectricBlue)
                            }
                        }
                    }
                    is UiState.Error -> {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = postsState.message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    is UiState.Success -> {
                        val filteredPosts = postsState.data.filter { post ->
                            when (filterChip) {
                                "Following" -> followingIds.contains(post.userId) || post.userId == currentUserId
                                "Photos" -> post.mediaType == "photo"
                                "Videos" -> post.mediaType == "video"
                                else -> true
                            }
                        }

                        if (filteredPosts.isEmpty()) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(22.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = VibeElectricBlue,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = "Your VIBEMATE feed is ready",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = "Publish a real photo/video post via Create, or publish the 3 curated starter vibes directly to Cloud Firestore.",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        VibePrimaryGradientButton(
                                            text = "Publish Starter Showcase",
                                            loading = isBusy,
                                            enabled = currentUserProfile != null,
                                            onClick = onSeedShowcase,
                                            testTag = "seed_showcase_button"
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredPosts, key = { it.id }) { post ->
                                VibePostCard(
                                    post = post,
                                    isLiked = likedPostIds.contains(post.id),
                                    isBookmarked = bookmarkedPostIds.contains(post.id),
                                    isFollowingAuthor = followingIds.contains(post.userId),
                                    isOwnPost = post.userId == currentUserId,
                                    onLikeClick = { onLikePost(post) },
                                    onCommentClick = { onCommentPost(post) },
                                    onBookmarkClick = { onBookmarkPost(post) },
                                    onShareClick = { onSharePost(post) },
                                    onFollowClick = { onFollowAuthor(post) },
                                    onReportClick = { onReportPost(post) }
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}
