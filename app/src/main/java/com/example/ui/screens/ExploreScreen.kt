package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VibePost
import com.example.ui.components.VibeAvatar
import com.example.ui.components.VibeMediaSurface
import com.example.ui.components.VibeVerifiedBadge
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibeObsidian
import com.example.ui.theme.VibePurple
import com.example.ui.theme.VibeRose

@Composable
fun ExploreScreen(
    currentUserId: String,
    searchQuery: String,
    selectedCategory: String,
    postsState: UiState<List<VibePost>>,
    usersState: UiState<List<UserProfile>>,
    followingIds: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleFollow: (UserProfile) -> Unit,
    onOpenReels: () -> Unit
) {
    val categories = listOf("All", "Photos", "Reels", "Studio", "Motion")

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1040.dp)
        ) {
            val columns = when {
                maxWidth >= 840.dp -> 4
                maxWidth >= 600.dp -> 3
                else -> 2
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Header & Search Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search creators, @handles, #tags, or vibes") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = VibeElectricBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibeElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_search_input")
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val selected = selectedCategory == cat
                            Surface(
                                onClick = { onSelectCategory(cat) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (selected) VibePurple else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) VibeElectricBlue else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                val q = searchQuery.trim().lowercase()
                val creators = ((usersState as? UiState.Success)?.data ?: emptyList()).filter { user ->
                    q.isBlank() ||
                        user.displayName.lowercase().contains(q) ||
                        user.handle.lowercase().contains(q) ||
                        user.bio.lowercase().contains(q)
                }

                when (postsState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VibeElectricBlue)
                        }
                    }
                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(postsState.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is UiState.Success -> {
                        val filteredPosts = postsState.data.filter { post ->
                            val matchesCat = when (selectedCategory) {
                                "Photos" -> post.mediaType == "photo"
                                "Reels" -> post.postType == "reel" || post.mediaType == "video"
                                "Studio" -> post.hashtags.any { it.contains("studio") } || post.mediaKey == "studio"
                                "Motion" -> post.hashtags.any { it.contains("motion") } || post.postType == "reel"
                                else -> true
                            }
                            val matchesQuery = q.isBlank() ||
                                post.caption.lowercase().contains(q) ||
                                post.authorHandle.lowercase().contains(q) ||
                                post.authorName.lowercase().contains(q) ||
                                post.hashtags.any { it.lowercase().contains(q) }
                            matchesCat && matchesQuery
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (creators.isNotEmpty()) {
                                item(span = { GridItemSpan(columns) }) {
                                    Column {
                                        Text(
                                            text = "Creators to Connect With",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            items(creators, key = { it.userId }) { user ->
                                                val isFollowing = followingIds.contains(user.userId)
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(18.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                                    modifier = Modifier.width(172.dp)
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.padding(14.dp)
                                                    ) {
                                                        VibeAvatar(
                                                            displayName = user.displayName,
                                                            preset = user.avatarPreset,
                                                            size = 48.dp,
                                                            showRing = true
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = user.displayName,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (user.verified || user.founderBadge) {
                                                                Spacer(Modifier.width(4.dp))
                                                                VibeVerifiedBadge(founder = user.founderBadge)
                                                            }
                                                        }
                                                        Text(
                                                            text = "@${user.handle}",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            text = "${user.followersCount} followers",
                                                            style = MaterialTheme.typography.labelSmall.copy(color = VibeElectricBlue)
                                                        )
                                                        if (user.userId != currentUserId) {
                                                            Spacer(Modifier.height(10.dp))
                                                            Surface(
                                                                onClick = { onToggleFollow(user) },
                                                                shape = RoundedCornerShape(14.dp),
                                                                color = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else VibePurple,
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Text(
                                                                    text = if (isFollowing) "Following" else "Follow",
                                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                                        color = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White,
                                                                        fontWeight = FontWeight.SemiBold
                                                                    ),
                                                                    modifier = Modifier.padding(vertical = 7.dp),
                                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

                            item(span = { GridItemSpan(columns) }) {
                                Text(
                                    text = "Trending Vibes (${filteredPosts.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                                )
                            }

                            if (filteredPosts.isEmpty()) {
                                item(span = { GridItemSpan(columns) }) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(18.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "No matching posts or reels yet. Try another search or create a vibe!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(20.dp)
                                        )
                                    }
                                }
                            } else {
                                items(filteredPosts, key = { it.id }) { post ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (post.postType == "reel" || post.mediaType == "video") {
                                                    onOpenReels()
                                                }
                                            }
                                    ) {
                                        VibeMediaSurface(
                                            mediaKey = post.mediaKey,
                                            mediaType = post.mediaType,
                                            contentDescription = post.caption.ifBlank { "Explore vibe" },
                                            aspectRatio = 3f / 4f
                                        )

                                        if (post.postType == "reel" || post.mediaType == "video") {
                                            Icon(
                                                imageVector = Icons.Filled.Videocam,
                                                contentDescription = "Reel",
                                                tint = VibeElectricBlue,
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp)
                                                    .size(18.dp)
                                            )
                                        }

                                        Surface(
                                            color = VibeObsidian.copy(alpha = 0.72f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Favorite,
                                                    contentDescription = null,
                                                    tint = VibeRose,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = "${post.likesCount} • @${post.authorHandle}",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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
        }
    }
}
