package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.PostComment
import com.example.data.model.UiState
import com.example.data.model.VibePost
import com.example.ui.theme.VibeCobalt
import com.example.ui.theme.VibeDeepViolet
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibeGradients
import com.example.ui.theme.VibeObsidian
import com.example.ui.theme.VibePurple
import com.example.ui.theme.VibeRose
import com.example.ui.theme.VibeWhite
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay

@Composable
fun VibePrismMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val prismPath = Path().apply {
            moveTo(w * 0.16f, h * 0.22f)
            lineTo(w * 0.50f, h * 0.84f)
            lineTo(w * 0.84f, h * 0.22f)
        }
        val innerWave = Path().apply {
            moveTo(w * 0.30f, h * 0.24f)
            lineTo(w * 0.50f, h * 0.58f)
            lineTo(w * 0.70f, h * 0.24f)
        }
        drawPath(
            path = prismPath,
            brush = Brush.linearGradient(
                colors = listOf(VibePurple, VibeCobalt, VibeElectricBlue),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            style = Stroke(width = w * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = innerWave,
            color = VibeElectricBlue,
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(
            color = VibeWhite,
            radius = w * 0.065f,
            center = Offset(w * 0.50f, h * 0.20f)
        )
    }
}

@Composable
fun VibeAvatar(
    displayName: String,
    preset: String,
    size: Dp = 42.dp,
    showRing: Boolean = false
) {
    val gradient = when (preset) {
        "electric_cyan" -> Brush.linearGradient(listOf(VibeCobalt, VibeElectricBlue))
        "obsidian_neon" -> Brush.linearGradient(listOf(VibeDeepViolet, VibeElectricBlue))
        "rose_prism" -> Brush.linearGradient(listOf(VibeRose, VibePurple))
        else -> VibeGradients.PrismBrand
    }
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "V"

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(if (showRing) size + 6.dp else size)
            .then(
                if (showRing) {
                    Modifier
                        .clip(CircleShape)
                        .border(2.dp, VibeGradients.PrismBrand, CircleShape)
                        .padding(3.dp)
                } else Modifier
            )
            .clip(CircleShape)
            .background(gradient)
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium.copy(
                color = VibeWhite,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun VibeVerifiedBadge(founder: Boolean = false) {
    if (founder) {
        Surface(
            color = VibeDeepViolet.copy(alpha = 0.25f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, VibeElectricBlue.copy(alpha = 0.6f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Founder badge",
                    tint = VibeElectricBlue,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "FOUNDER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = VibeElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    } else {
        Icon(
            imageVector = Icons.Filled.Verified,
            contentDescription = "Verified creator",
            tint = VibeElectricBlue,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun VibePrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    testTag: String = "vibe_gradient_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(52.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) VibeGradients.PrismBrand else Brush.linearGradient(
                        listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = VibeWhite,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(color = VibeWhite)
                )
            }
        }
    }
}

@Composable
fun VibeMediaSurface(
    mediaKey: String,
    mediaType: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 4f / 3f
) {
    val drawableRes = when (mediaKey) {
        "studio" -> R.drawable.img_vibe_studio
        "horizon" -> R.drawable.img_vibe_horizon
        "reel_motion" -> R.drawable.img_vibe_reel
        "splash" -> R.drawable.img_vibe_splash
        "logo" -> R.drawable.img_vibemate_logo
        else -> null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(18.dp))
            .background(VibeObsidian)
    ) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (mediaKey.startsWith("content://") || mediaKey.startsWith("http")) {
            AsyncImage(
                model = Uri.parse(mediaKey),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_vibe_studio),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (mediaType == "video") {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(VibeObsidian.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
                    .border(1.dp, VibeElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = "Video post",
                        tint = VibeElectricBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "VIDEO",
                        style = MaterialTheme.typography.labelSmall.copy(color = VibeWhite)
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .background(VibeObsidian.copy(alpha = 0.65f), CircleShape)
                    .border(1.5.dp, VibeWhite.copy(alpha = 0.75f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play video",
                    tint = VibeWhite,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun VibePostCard(
    post: VibePost,
    isLiked: Boolean,
    isBookmarked: Boolean,
    isFollowingAuthor: Boolean,
    isOwnPost: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showHeartBurst by remember { mutableStateOf(false) }

    LaunchedEffect(showHeartBurst) {
        if (showHeartBurst) {
            delay(700)
            showHeartBurst = false
        }
    }

    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "likeScale"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Author Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                VibeAvatar(
                    displayName = post.authorName,
                    preset = post.authorAvatarPreset,
                    size = 42.dp,
                    showRing = true
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (post.authorVerified) {
                            Spacer(Modifier.width(5.dp))
                            VibeVerifiedBadge(founder = false)
                        }
                    }
                    Text(
                        text = "@${post.authorHandle} • ${formatRelativeTime(post.createdAt)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (!isOwnPost) {
                    Surface(
                        onClick = onFollowClick,
                        shape = RoundedCornerShape(20.dp),
                        color = if (isFollowingAuthor) MaterialTheme.colorScheme.surfaceVariant else VibePurple.copy(alpha = 0.2f),
                        border = BorderStroke(
                            1.dp,
                            if (isFollowingAuthor) MaterialTheme.colorScheme.outline else VibePurple
                        ),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(
                            text = if (isFollowingAuthor) "Following" else "Follow",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isFollowingAuthor) MaterialTheme.colorScheme.onSurface else VibeElectricBlue
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onReportClick,
                    modifier = Modifier.testTag("report_post_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "Report post",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Media with Double-Tap Heart Burst
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(post.id, isLiked) {
                        detectTapGestures(
                            onDoubleTap = {
                                showHeartBurst = true
                                if (!isLiked) onLikeClick()
                            }
                        )
                    }
            ) {
                VibeMediaSurface(
                    mediaKey = post.mediaKey,
                    mediaType = post.mediaType,
                    contentDescription = post.caption.ifBlank { "VIBEMATE post" },
                    aspectRatio = if (post.postType == "reel") 4f / 4.6f else 4f / 3f
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = showHeartBurst,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = VibeRose,
                        modifier = Modifier.size(84.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) VibeRose else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.scale(likeScale)
                    )
                }
                Text(
                    text = "${post.likesCount}",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(Modifier.width(12.dp))

                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubbleOutline,
                        contentDescription = "Comments"
                    )
                }
                Text(
                    text = "${post.commentsCount}",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        onShareClick()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out @${post.authorHandle} on VIBEMATE: ${post.caption}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via VIBEMATE"))
                    },
                    modifier = Modifier.testTag("share_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share"
                    )
                }
                if (post.sharesCount > 0) {
                    Text(
                        text = "${post.sharesCount}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.testTag("bookmark_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) VibeElectricBlue else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (post.hashtags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = post.hashtags.joinToString(" ") { "#$it" },
                    style = MaterialTheme.typography.labelMedium.copy(color = VibeElectricBlue),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    post: VibePost,
    commentsState: UiState<List<PostComment>>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSendComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Comments on @${post.authorHandle}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 320.dp)
            ) {
                when (commentsState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is UiState.Error -> {
                        Text(
                            text = commentsState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is UiState.Success -> {
                        if (commentsState.data.isEmpty()) {
                            Text(
                                text = "Start the conversation on this vibe.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(commentsState.data, key = { it.id }) { comment ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        VibeAvatar(
                                            displayName = comment.authorName,
                                            preset = "electric_cyan",
                                            size = 32.dp
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "@${comment.authorHandle}",
                                                style = MaterialTheme.typography.labelLarge.copy(color = VibeElectricBlue)
                                            )
                                            Text(
                                                text = comment.text,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { if (it.length <= 300) commentText = it },
                    placeholder = { Text("Add a comment...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input")
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val trimmed = commentText.trim()
                        if (trimmed.isNotEmpty()) {
                            onSendComment(trimmed)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank() && !isBusy,
                    modifier = Modifier.testTag("send_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send comment",
                        tint = VibeElectricBlue
                    )
                }
            }
        }
    }
}

@Composable
fun ReportPostDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    val quickReasons = listOf("Spam or misleading", "Harassment", "Inappropriate media")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report to Moderation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Reports are reviewed securely in the Founder Admin Dashboard.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickReasons.forEach { preset ->
                        Surface(
                            onClick = { reason = preset },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = { if (it.length <= 250) reason = it },
                    label = { Text("Reason") },
                    placeholder = { Text("Describe the issue (min 3 chars)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason.trim()) },
                enabled = reason.trim().length >= 3
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatRelativeTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "Just now"
    val diffSec = ((System.currentTimeMillis() - timestamp.toDate().time) / 1000L).coerceAtLeast(0L)
    return when {
        diffSec < 60 -> "Just now"
        diffSec < 3600 -> "${diffSec / 60}m"
        diffSec < 86400 -> "${diffSec / 3600}h"
        else -> "${diffSec / 86400}d"
    }
}
