package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val handle: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarPreset: String = "prism_violet",
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val postsCount: Long = 0L,
    val verified: Boolean = false,
    val founderBadge: Boolean = false,
    val discoverable: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminRole(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "founder",
    @ServerTimestamp val grantedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VibePost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorHandle: String = "",
    val authorName: String = "",
    val authorAvatarPreset: String = "prism_violet",
    val authorVerified: Boolean = false,
    val postType: String = "post",
    val mediaType: String = "photo",
    val mediaKey: String = "studio",
    val caption: String = "",
    val hashtags: List<String> = emptyList(),
    val audioTitle: String = "",
    val visibility: String = "public",
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val sharesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PostComment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorHandle: String = "",
    val authorName: String = "",
    val postVisibility: String = "public",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FollowEdge(
    @DocumentId val id: String = "",
    val followerId: String = "",
    val targetUserId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookmarkItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class NotificationItem(
    @DocumentId val id: String = "",
    val targetUserId: String = "",
    val actorId: String = "",
    val actorHandle: String = "",
    val actorName: String = "",
    val type: String = "system",
    val message: String = "",
    val postId: String? = null,
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ModerationReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetPostId: String = "",
    val reason: String = "",
    val status: String = "open",
    val resolutionNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
