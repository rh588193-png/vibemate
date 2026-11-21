package com.example.data.repository

import android.content.Context
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.firebase.resolveAppletFirestore
import com.example.data.model.AdminRole
import com.example.data.model.BookmarkItem
import com.example.data.model.FollowEdge
import com.example.data.model.ModerationReport
import com.example.data.model.NotificationItem
import com.example.data.model.PostComment
import com.example.data.model.UserProfile
import com.example.data.model.VibePost
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class VibeRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(resolveAppletFirestore(context))

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun <T> Flow<T>.logFirestoreErrors(op: OperationType, path: String, uid: String): Flow<T> =
        catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val toLog = firestoreError ?: (error as? Exception)
            if (toLog != null && auth.currentUser?.uid == uid) {
                handleFirestoreError(toLog, op, path)
            }
            throw error
        }

    fun observePublicPosts(currentUserId: String): Flow<List<VibePost>> {
        val path = "posts"
        return db.collection(path)
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snap ->
                snap.toObjects(VibePost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .logFirestoreErrors(OperationType.LIST, path, currentUserId)
    }

    fun observeDiscoverableUsers(currentUserId: String): Flow<List<UserProfile>> {
        val path = "users"
        return db.collection(path)
            .whereEqualTo("discoverable", true)
            .snapshots()
            .map { snap ->
                snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.followersCount }
            }
            .logFirestoreErrors(OperationType.LIST, path, currentUserId)
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else null
            }
            .logFirestoreErrors(OperationType.GET, path, userId)
    }

    fun observeAdminRole(userId: String): Flow<AdminRole?> {
        val path = "admins/$userId"
        return db.collection("admins").document(userId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(AdminRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else null
            }
            .logFirestoreErrors(OperationType.GET, path, userId)
    }

    fun observeUserBookmarks(userId: String): Flow<Set<String>> {
        val path = "users/$userId/bookmarks"
        return db.collection("users").document(userId).collection("bookmarks")
            .snapshots()
            .map { snap ->
                snap.toObjects(BookmarkItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .map { it.postId }
                    .toSet()
            }
            .logFirestoreErrors(OperationType.LIST, path, userId)
    }

    fun observeUserFollowingIds(userId: String): Flow<Set<String>> {
        val path = "follows"
        return db.collection(path)
            .whereEqualTo("followerId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(FollowEdge::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .map { it.targetUserId }
                    .toSet()
            }
            .logFirestoreErrors(OperationType.LIST, path, userId)
    }

    fun observeUserNotifications(userId: String): Flow<List<NotificationItem>> {
        val path = "users/$userId/notifications"
        return db.collection("users").document(userId).collection("notifications")
            .snapshots()
            .map { snap ->
                snap.toObjects(NotificationItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .logFirestoreErrors(OperationType.LIST, path, userId)
    }

    fun observePostComments(postId: String, currentUserId: String): Flow<List<PostComment>> {
        val path = "posts/$postId/comments"
        return db.collection("posts").document(postId).collection("comments")
            .whereEqualTo("postVisibility", "public")
            .snapshots()
            .map { snap ->
                snap.toObjects(PostComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.createdAt ?: Timestamp(0, 0) }
            }
            .logFirestoreErrors(OperationType.LIST, path, currentUserId)
    }

    fun observeModerationReports(currentUserId: String): Flow<List<ModerationReport>> {
        val path = "moderation_reports"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(ModerationReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .logFirestoreErrors(OperationType.LIST, path, currentUserId)
    }

    suspend fun isPostLikedByUser(postId: String, userId: String): Boolean {
        val path = "posts/$postId/likes/$userId"
        return try {
            db.collection("posts").document(postId).collection("likes").document(userId)
                .get().await().exists()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            false
        }
    }

    suspend fun ensureUserProfile(
        preferredHandle: String,
        preferredDisplayName: String,
        bio: String = "Creating & connecting on VIBEMATE.",
        avatarPreset: String = "prism_violet"
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return try {
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val safeName = preferredDisplayName.trim().ifBlank {
                    auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Creator"
                }.take(50)
                val sanitizedHandle = preferredHandle.trim().lowercase()
                    .replace(Regex("[^a-z0-9_.]"), "")
                    .ifBlank { "vibe_${uid.take(6).lowercase()}" }
                    .take(32)
                    .let { if (it.length < 2) "vm_$it" else it }
                val payload = mapOf(
                    "userId" to uid,
                    "handle" to sanitizedHandle,
                    "displayName" to safeName,
                    "bio" to bio.trim().take(200),
                    "avatarPreset" to avatarPreset.take(64),
                    "followersCount" to 0L,
                    "followingCount" to 0L,
                    "postsCount" to 0L,
                    "verified" to false,
                    "founderBadge" to false,
                    "discoverable" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        displayName: String,
        handle: String,
        bio: String,
        avatarPreset: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return try {
            requireUserId()
            val cleanName = displayName.trim()
            require(cleanName.length in 1..50) { "Display name must be 1 to 50 characters." }
            val cleanHandle = handle.trim().lowercase().replace(Regex("[^a-z0-9_.]"), "")
            require(cleanHandle.length in 2..32) { "Handle must be 2 to 32 characters (letters, numbers, _ or .)." }
            val cleanBio = bio.trim().take(200)

            db.collection("users").document(uid).update(
                mapOf(
                    "displayName" to cleanName,
                    "handle" to cleanHandle,
                    "bio" to cleanBio,
                    "avatarPreset" to avatarPreset.take(64),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun claimFounderAdminAccess(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "admins/$uid"
        return try {
            requireUserId()
            val adminRef = db.collection("admins").document(uid)
            if (!adminRef.get().await().exists()) {
                adminRef.set(
                    mapOf(
                        "userId" to uid,
                        "role" to "founder",
                        "grantedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            db.collection("users").document(uid).update(
                mapOf(
                    "founderBadge" to true,
                    "verified" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun createPost(
        author: UserProfile,
        postType: String,
        mediaType: String,
        mediaKey: String,
        caption: String,
        hashtagsRaw: String,
        audioTitle: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val postRef = db.collection("posts").document()
        val path = postRef.path
        return try {
            requireUserId()
            require(postType in listOf("post", "reel")) { "Invalid post format." }
            require(mediaType in listOf("photo", "video")) { "Invalid media type." }
            val cleanCaption = caption.trim()
            require(cleanCaption.length <= 500) { "Caption cannot exceed 500 characters." }

            val tags = hashtagsRaw.split(Regex("[\\s,]+"))
                .map { it.trim().removePrefix("#").lowercase() }
                .filter { it.isNotBlank() }
                .distinct()
                .take(10)

            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "authorHandle" to author.handle.ifBlank { "creator" }.take(32),
                "authorName" to author.displayName.ifBlank { "Creator" }.take(50),
                "authorAvatarPreset" to author.avatarPreset.take(64),
                "authorVerified" to author.verified,
                "postType" to postType,
                "mediaType" to mediaType,
                "mediaKey" to mediaKey.take(500),
                "caption" to cleanCaption,
                "hashtags" to tags,
                "visibility" to "public",
                "likesCount" to 0L,
                "commentsCount" to 0L,
                "sharesCount" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (audioTitle.isNotBlank()) {
                payload["audioTitle"] = audioTitle.trim().take(100)
            }

            postRef.set(payload).await()
            db.collection("users").document(uid).update(
                mapOf(
                    "postsCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun seedLaunchShowcase(author: UserProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return try {
            requireUserId()
            val samples = listOf(
                Triple(
                    "post",
                    "photo" to "studio",
                    "Late-night lighting geometry inside the VIBEMATE creative studio. Built for creators who move culture forward."
                ),
                Triple(
                    "post",
                    "photo" to "horizon",
                    "Twilight reflections above the skyline. Create. Share. Connect."
                ),
                Triple(
                    "reel",
                    "video" to "reel_motion",
                    "Kinetic light-trail choreography session — full motion drop now live on VIBEMATE Reels."
                )
            )
            samples.forEachIndexed { idx, (pType, mediaPair, text) ->
                val ref = db.collection("posts").document()
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "authorHandle" to author.handle.ifBlank { "creator" }.take(32),
                    "authorName" to author.displayName.ifBlank { "Creator" }.take(50),
                    "authorAvatarPreset" to author.avatarPreset.take(64),
                    "authorVerified" to author.verified,
                    "postType" to pType,
                    "mediaType" to mediaPair.first,
                    "mediaKey" to mediaPair.second,
                    "caption" to text,
                    "hashtags" to listOf("vibemate", if (pType == "reel") "motion" else "studio", "create"),
                    "visibility" to "public",
                    "likesCount" to 0L,
                    "commentsCount" to 0L,
                    "sharesCount" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (pType == "reel") {
                    payload["audioTitle"] = "Prism Pulse (Original Sound) • ${author.displayName}"
                }
                ref.set(payload).await()
                db.collection("users").document(uid).update(
                    mapOf(
                        "postsCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "posts")
            Result.failure(e)
        }
    }

    suspend fun toggleLike(post: VibePost, actor: UserProfile, currentlyLiked: Boolean): Result<Boolean> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val likeRef = db.collection("posts").document(post.id).collection("likes").document(uid)
        val postRef = db.collection("posts").document(post.id)
        return try {
            requireUserId()
            require(post.id.isNotBlank())
            if (currentlyLiked) {
                likeRef.delete().await()
                postRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(-1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(false)
            } else {
                likeRef.set(
                    mapOf(
                        "userId" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                postRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                if (post.userId != uid) {
                    sendNotification(
                        targetUserId = post.userId,
                        actor = actor,
                        type = "like",
                        message = "${actor.displayName} liked your ${post.postType}",
                        postId = post.id
                    )
                }
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, likeRef.path)
            Result.failure(e)
        }
    }

    suspend fun addComment(post: VibePost, actor: UserProfile, text: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val cleanText = text.trim()
        if (cleanText.isEmpty() || cleanText.length > 300) {
            return Result.failure(IllegalArgumentException("Comment must be 1 to 300 characters."))
        }
        val commentRef = db.collection("posts").document(post.id).collection("comments").document()
        return try {
            requireUserId()
            commentRef.set(
                mapOf(
                    "userId" to uid,
                    "authorHandle" to actor.handle.ifBlank { "creator" }.take(32),
                    "authorName" to actor.displayName.ifBlank { "Creator" }.take(50),
                    "postVisibility" to "public",
                    "text" to cleanText,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            db.collection("posts").document(post.id).update(
                mapOf(
                    "commentsCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (post.userId != uid) {
                sendNotification(
                    targetUserId = post.userId,
                    actor = actor,
                    type = "comment",
                    message = "${actor.displayName} commented: \"${cleanText.take(60)}\"",
                    postId = post.id
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleBookmark(postId: String, currentlyBookmarked: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val ref = db.collection("users").document(uid).collection("bookmarks").document(postId)
        return try {
            requireUserId()
            if (currentlyBookmarked) {
                ref.delete().await()
            } else {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "postId" to postId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun toggleFollow(targetUser: UserProfile, actor: UserProfile, currentlyFollowing: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        if (targetUser.userId == uid) return Result.success(Unit)
        val followId = "${uid}_${targetUser.userId}"
        val followRef = db.collection("follows").document(followId)
        return try {
            requireUserId()
            if (currentlyFollowing) {
                followRef.delete().await()
                db.collection("users").document(uid).update(
                    mapOf("followingCount" to FieldValue.increment(-1L), "updatedAt" to FieldValue.serverTimestamp())
                ).await()
                db.collection("users").document(targetUser.userId).update(
                    mapOf("followersCount" to FieldValue.increment(-1L), "updatedAt" to FieldValue.serverTimestamp())
                ).await()
            } else {
                followRef.set(
                    mapOf(
                        "followerId" to uid,
                        "targetUserId" to targetUser.userId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                db.collection("users").document(uid).update(
                    mapOf("followingCount" to FieldValue.increment(1L), "updatedAt" to FieldValue.serverTimestamp())
                ).await()
                db.collection("users").document(targetUser.userId).update(
                    mapOf("followersCount" to FieldValue.increment(1L), "updatedAt" to FieldValue.serverTimestamp())
                ).await()
                sendNotification(
                    targetUserId = targetUser.userId,
                    actor = actor,
                    type = "follow",
                    message = "${actor.displayName} started following you",
                    postId = null
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, followRef.path)
            Result.failure(e)
        }
    }

    suspend fun recordShare(postId: String): Result<Unit> {
        val path = "posts/$postId"
        return try {
            requireUserId()
            db.collection("posts").document(postId).update(
                mapOf(
                    "sharesCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/notifications/$notificationId"
        return try {
            requireUserId()
            db.collection("users").document(uid).collection("notifications").document(notificationId)
                .update(
                    mapOf(
                        "read" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun submitReport(targetPostId: String, reason: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val cleanReason = reason.trim()
        if (cleanReason.length < 3 || cleanReason.length > 250) {
            return Result.failure(IllegalArgumentException("Reason must be 3 to 250 characters."))
        }
        val ref = db.collection("moderation_reports").document()
        return try {
            requireUserId()
            ref.set(
                mapOf(
                    "reporterId" to uid,
                    "targetPostId" to targetPostId,
                    "reason" to cleanReason,
                    "status" to "open",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun resolveReport(
        reportId: String,
        targetPostId: String,
        newStatus: String,
        removePost: Boolean,
        note: String
    ): Result<Unit> {
        val path = "moderation_reports/$reportId"
        return try {
            requireUserId()
            require(newStatus in listOf("resolved", "dismissed"))
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (note.isNotBlank()) {
                updates["resolutionNote"] = note.trim().take(250)
            }
            db.collection("moderation_reports").document(reportId).update(updates).await()
            if (removePost && targetPostId.isNotBlank()) {
                db.collection("posts").document(targetPostId).update(
                    mapOf(
                        "visibility" to "removed",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleUserVerified(targetUserId: String, verified: Boolean): Result<Unit> {
        val path = "users/$targetUserId"
        return try {
            requireUserId()
            db.collection("users").document(targetUserId).update(
                mapOf(
                    "verified" to verified,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    private suspend fun sendNotification(
        targetUserId: String,
        actor: UserProfile,
        type: String,
        message: String,
        postId: String?
    ) {
        val uid = auth.currentUser?.uid ?: return
        val notifRef = db.collection("users").document(targetUserId).collection("notifications").document()
        val payload = mutableMapOf<String, Any>(
            "targetUserId" to targetUserId,
            "actorId" to uid,
            "actorHandle" to actor.handle.ifBlank { "creator" }.take(32),
            "actorName" to actor.displayName.ifBlank { "Creator" }.take(50),
            "type" to type,
            "message" to message.take(240),
            "read" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!postId.isNullOrBlank()) {
            payload["postId"] = postId
        }
        try {
            notifRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, notifRef.path)
        }
    }
}
