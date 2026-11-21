package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UiState
import com.example.data.model.VibePost
import com.example.ui.components.VibeAvatar
import com.example.ui.components.VibeMediaSurface
import com.example.ui.components.VibePrimaryGradientButton
import com.example.ui.components.VibeVerifiedBadge
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibeGradients
import com.example.ui.theme.VibeObsidian
import com.example.ui.theme.VibePurple
import com.example.ui.theme.VibeRose
import com.example.ui.theme.VibeWhite

@Composable
fun ReelsScreen(
    currentUserId: String,
    postsState: UiState<List<VibePost>>,
    likedPostIds: Set<String>,
    bookmarkedPostIds: Set<String>,
    followingIds: Set<String>,
    onLikePost: (VibePost) -> Unit,
    onCommentPost: (VibePost) -> Unit,
    onBookmarkPost: (VibePost) -> Unit,
    onSharePost: (VibePost) -> Unit,
    onFollowAuthor: (VibePost) -> Unit,
    onReportPost: (VibePost) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(VibeObsidian)
    ) {
        when (postsState) {
            is UiState.Loading -> CircularProgressIndicator(color = VibeElectricBlue)
            is UiState.Error -> Text(postsState.message, color = VibeRose)
            is UiState.Success -> {
                val reels = postsState.data.filter { it.postType == "reel" || it.mediaType == "video" }
                if (reels.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = null,
                            tint = VibeElectricBlue,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "VIBEMATE Reels",
                            style = MaterialTheme.typography.headlineMedium.copy(color = VibeWhite)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Drop a vertical reel from Create or publish the starter showcase from Home to launch the Reels stream.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.72f))
                        )
                        Spacer(Modifier.height(18.dp))
                        VibePrimaryGradientButton(
                            text = "Create a Reel",
                            onClick = onNavigateToCreate,
                            testTag = "reels_empty_create_button"
                        )
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { reels.size })
                    var isPlaying by remember { mutableStateOf(true) }
                    var isMuted by remember { mutableStateOf(false) }

                    val discTransition = rememberInfiniteTransition(label = "discSpin")
                    val discRotation by discTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "discAngle"
                    )

                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 520.dp)
                            .testTag("reels_vertical_pager")
                    ) { page ->
                        val reel = reels[page]
                        val isLiked = likedPostIds.contains(reel.id)
                        val isBookmarked = bookmarkedPostIds.contains(reel.id)
                        val isFollowing = followingIds.contains(reel.userId)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(VibeObsidian)
                                .clickable { isPlaying = !isPlaying }
                        ) {
                            VibeMediaSurface(
                                mediaKey = reel.mediaKey,
                                mediaType = "photo", // Custom overlay below handles kinetic Reel controls
                                contentDescription = reel.caption.ifBlank { "VIBEMATE Reel" },
                                aspectRatio = 9f / 16f,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(VibeGradients.DarkScrim)
                            )

                            // Top Reel Bar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp)
                            ) {
                                Surface(
                                    color = VibeObsidian.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, VibeElectricBlue.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "REELS • ${page + 1}/${reels.size}",
                                        style = MaterialTheme.typography.labelMedium.copy(color = VibeElectricBlue),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { isMuted = !isMuted },
                                    modifier = Modifier
                                        .background(VibeObsidian.copy(alpha = 0.6f), CircleShape)
                                        .testTag("reel_mute_button")
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isMuted) "Unmute" else "Mute",
                                        tint = VibeWhite
                                    )
                                }
                            }

                            // Center Play/Pause indicator when paused
                            if (!isPlaying) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(72.dp)
                                        .background(VibeObsidian.copy(alpha = 0.7f), CircleShape)
                                        .border(2.dp, VibeElectricBlue, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Resume",
                                        tint = VibeWhite,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            // Right Action Rail
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 14.dp, bottom = 24.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { onLikePost(reel) },
                                        modifier = Modifier.background(VibeObsidian.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Like reel",
                                            tint = if (isLiked) VibeRose else VibeWhite
                                        )
                                    }
                                    Text(
                                        text = "${reel.likesCount}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = VibeWhite)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { onCommentPost(reel) },
                                        modifier = Modifier.background(VibeObsidian.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ChatBubbleOutline,
                                            contentDescription = "Comment",
                                            tint = VibeWhite
                                        )
                                    }
                                    Text(
                                        text = "${reel.commentsCount}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = VibeWhite)
                                    )
                                }

                                IconButton(
                                    onClick = { onBookmarkPost(reel) },
                                    modifier = Modifier.background(VibeObsidian.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = "Save reel",
                                        tint = if (isBookmarked) VibeElectricBlue else VibeWhite
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onSharePost(reel)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Watch @${reel.authorHandle}'s Reel on VIBEMATE: ${reel.caption}")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Reel"))
                                    },
                                    modifier = Modifier.background(VibeObsidian.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Share reel",
                                        tint = VibeWhite
                                    )
                                }

                                IconButton(
                                    onClick = { onReportPost(reel) },
                                    modifier = Modifier.background(VibeObsidian.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Flag,
                                        contentDescription = "Report reel",
                                        tint = VibeWhite.copy(alpha = 0.8f)
                                    )
                                }

                                // Kinetic Audio Disc
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .rotate(if (isPlaying) discRotation else 0f)
                                        .clip(CircleShape)
                                        .background(VibeGradients.PrismBrand)
                                        .border(2.dp, VibeWhite, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.MusicNote else Icons.Filled.Pause,
                                        contentDescription = "Audio track",
                                        tint = VibeWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Bottom Creator & Caption Overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(0.76f)
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    VibeAvatar(
                                        displayName = reel.authorName,
                                        preset = reel.authorAvatarPreset,
                                        size = 38.dp,
                                        showRing = true
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "@${reel.authorHandle}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = VibeWhite,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (reel.authorVerified) {
                                        Spacer(Modifier.width(5.dp))
                                        VibeVerifiedBadge()
                                    }
                                    if (reel.userId != currentUserId) {
                                        Spacer(Modifier.width(10.dp))
                                        Surface(
                                            onClick = { onFollowAuthor(reel) },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isFollowing) VibeObsidian.copy(alpha = 0.7f) else VibePurple,
                                            border = BorderStroke(1.dp, VibeWhite.copy(alpha = 0.7f))
                                        ) {
                                            Text(
                                                text = if (isFollowing) "Following" else "Follow",
                                                style = MaterialTheme.typography.labelSmall.copy(color = VibeWhite),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = reel.caption,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = VibeWhite),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(Modifier.height(8.dp))

                                Surface(
                                    color = VibeObsidian.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MusicNote,
                                            contentDescription = null,
                                            tint = VibeElectricBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = reel.audioTitle.ifBlank { "VIBEMATE Original Sound" },
                                            style = MaterialTheme.typography.labelSmall.copy(color = VibeWhite),
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
