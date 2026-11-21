package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.firebase.resolveAppletFirestore
import com.example.data.model.AdminRole
import com.example.data.model.ModerationReport
import com.example.data.model.NotificationItem
import com.example.data.model.PostComment
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VibePost
import com.example.data.repository.VibeRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VibeViewModel(
    private val repository: VibeRepository,
    val currentUserId: String,
    initialHandle: String = "",
    initialDisplayName: String = ""
) : ViewModel() {

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedExploreCategory = MutableStateFlow("All")
    val selectedExploreCategory: StateFlow<String> = _selectedExploreCategory.asStateFlow()

    private val _activeCommentPostId = MutableStateFlow<String?>(null)

    private val _isActionBusy = MutableStateFlow(false)
    val isActionBusy: StateFlow<Boolean> = _isActionBusy.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    private fun <T> Flow<T>.toUiStateFlow(tag: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Stream error in $tag", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "Failed to load $tag"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val postsState: StateFlow<UiState<List<VibePost>>> =
        repository.observePublicPosts(currentUserId).toUiStateFlow("posts")

    val usersState: StateFlow<UiState<List<UserProfile>>> =
        repository.observeDiscoverableUsers(currentUserId).toUiStateFlow("creators")

    val currentUserProfile: StateFlow<UserProfile?> =
        repository.observeUserProfile(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing profile", firestoreError ?: error)
                emit(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    val adminRole: StateFlow<AdminRole?> =
        repository.observeAdminRole(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    val bookmarkedPostIds: StateFlow<Set<String>> =
        repository.observeUserBookmarks(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(emptySet())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptySet())

    val followingIds: StateFlow<Set<String>> =
        repository.observeUserFollowingIds(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(emptySet())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptySet())

    val notificationsState: StateFlow<UiState<List<NotificationItem>>> =
        repository.observeUserNotifications(currentUserId).toUiStateFlow("notifications")

    val activePostComments: StateFlow<UiState<List<PostComment>>> =
        _activeCommentPostId.flatMapLatest { postId ->
            if (postId.isNullOrBlank()) flowOf(UiState.Success(emptyList()))
            else repository.observePostComments(postId, currentUserId)
                .map<List<PostComment>, UiState<List<PostComment>>> { UiState.Success(it) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    emit(UiState.Error((firestoreError ?: error).localizedMessage ?: "Failed to load comments"))
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val moderationReportsState: StateFlow<UiState<List<ModerationReport>>> =
        adminRole.flatMapLatest { role ->
            if (role == null) flowOf(UiState.Success(emptyList()))
            else repository.observeModerationReports(currentUserId)
                .map<List<ModerationReport>, UiState<List<ModerationReport>>> { UiState.Success(it) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    emit(UiState.Error((firestoreError ?: error).localizedMessage ?: "Failed to load reports"))
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(
                preferredHandle = initialHandle,
                preferredDisplayName = initialDisplayName
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectExploreCategory(category: String) {
        _selectedExploreCategory.value = category
    }

    fun openCommentsForPost(postId: String?) {
        _activeCommentPostId.value = postId
    }

    fun syncLikeStateForPosts(posts: List<VibePost>) {
        viewModelScope.launch {
            val liked = mutableSetOf<String>()
            posts.take(25).forEach { post ->
                if (repository.isPostLikedByUser(post.id, currentUserId)) {
                    liked.add(post.id)
                }
            }
            _likedPostIds.value = liked
        }
    }

    fun toggleLike(post: VibePost) {
        val actor = currentUserProfile.value ?: return
        val currentlyLiked = _likedPostIds.value.contains(post.id)
        _likedPostIds.value = if (currentlyLiked) {
            _likedPostIds.value - post.id
        } else {
            _likedPostIds.value + post.id
        }
        viewModelScope.launch {
            repository.toggleLike(post, actor, currentlyLiked).onFailure { err ->
                _likedPostIds.value = if (currentlyLiked) {
                    _likedPostIds.value + post.id
                } else {
                    _likedPostIds.value - post.id
                }
                _messages.tryEmit(err.localizedMessage ?: "Unable to update like")
            }
        }
    }

    fun toggleBookmark(postId: String) {
        val currentlyBookmarked = bookmarkedPostIds.value.contains(postId)
        viewModelScope.launch {
            repository.toggleBookmark(postId, currentlyBookmarked)
                .onSuccess {
                    _messages.tryEmit(if (currentlyBookmarked) "Removed from saved" else "Saved to your collection")
                }
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Unable to update bookmark")
                }
        }
    }

    fun toggleFollow(targetUser: UserProfile) {
        val actor = currentUserProfile.value ?: return
        val currentlyFollowing = followingIds.value.contains(targetUser.userId)
        viewModelScope.launch {
            repository.toggleFollow(targetUser, actor, currentlyFollowing)
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Unable to update follow status")
                }
        }
    }

    fun addComment(post: VibePost, text: String, onDone: () -> Unit = {}) {
        val actor = currentUserProfile.value ?: return
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.addComment(post, actor, text)
                .onSuccess { onDone() }
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Failed to post comment")
                }
            _isActionBusy.value = false
        }
    }

    fun createPost(
        postType: String,
        mediaType: String,
        mediaKey: String,
        caption: String,
        hashtagsRaw: String,
        audioTitle: String,
        onPublished: () -> Unit
    ) {
        val author = currentUserProfile.value
        if (author == null) {
            _messages.tryEmit("Initializing creator profile, please try again in a moment.")
            return
        }
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.createPost(author, postType, mediaType, mediaKey, caption, hashtagsRaw, audioTitle)
                .onSuccess {
                    _messages.tryEmit("Published to VIBEMATE!")
                    onPublished()
                }
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Failed to publish post")
                }
            _isActionBusy.value = false
        }
    }

    fun seedLaunchShowcase() {
        val author = currentUserProfile.value ?: return
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.seedLaunchShowcase(author)
                .onSuccess { _messages.tryEmit("Starter vibes published to Cloud Firestore!") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Failed to seed showcase") }
            _isActionBusy.value = false
        }
    }

    fun updateProfile(
        displayName: String,
        handle: String,
        bio: String,
        avatarPreset: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.updateProfile(displayName, handle, bio, avatarPreset)
                .onSuccess {
                    _messages.tryEmit("Profile updated")
                    onSaved()
                }
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Failed to update profile")
                }
            _isActionBusy.value = false
        }
    }

    fun claimFounderAdminAccess() {
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.claimFounderAdminAccess()
                .onSuccess { _messages.tryEmit("Founder Admin Console unlocked!") }
                .onFailure {
                    _messages.tryEmit("Founder verification is enforced server-side for the platform owner account.")
                }
            _isActionBusy.value = false
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun recordShare(postId: String) {
        viewModelScope.launch {
            repository.recordShare(postId)
        }
    }

    fun submitReport(postId: String, reason: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.submitReport(postId, reason)
                .onSuccess {
                    _messages.tryEmit("Report sent to Founder Moderation Queue")
                    onDone()
                }
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Failed to submit report")
                }
            _isActionBusy.value = false
        }
    }

    fun resolveReport(
        reportId: String,
        targetPostId: String,
        newStatus: String,
        removePost: Boolean,
        note: String
    ) {
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.resolveReport(reportId, targetPostId, newStatus, removePost, note)
                .onSuccess {
                    _messages.tryEmit(if (removePost) "Report resolved & post removed" else "Report marked $newStatus")
                }
                .onFailure { err ->
                    _messages.tryEmit(err.localizedMessage ?: "Admin action failed")
                }
            _isActionBusy.value = false
        }
    }

    fun toggleUserVerified(targetUserId: String, verified: Boolean) {
        viewModelScope.launch {
            repository.toggleUserVerified(targetUserId, verified)
                .onSuccess { _messages.tryEmit(if (verified) "Creator verified" else "Verification removed") }
                .onFailure { err -> _messages.tryEmit(err.localizedMessage ?: "Unable to update creator") }
        }
    }

    companion object {
        private const val TAG = "VibeVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun factory(
            currentUserId: String,
            initialHandle: String = "",
            initialDisplayName: String = ""
        ) = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val db = resolveAppletFirestore(app)
                VibeViewModel(
                    repository = VibeRepository(db),
                    currentUserId = currentUserId,
                    initialHandle = initialHandle,
                    initialDisplayName = initialDisplayName
                )
            }
        }
    }
}
