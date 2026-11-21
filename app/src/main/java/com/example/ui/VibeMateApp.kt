package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.auth.signOut
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VibePost
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.ReportPostDialog
import com.example.ui.components.VibePrismMark
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FounderAdminScreen
import com.example.ui.screens.HomeFeedScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.VIBEMATETheme
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibePurple
import com.example.ui.viewmodel.VibeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

private sealed class VibeDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : VibeDestination("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Reels : VibeDestination("reels", "Reels", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle)
    data object Explore : VibeDestination("explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore)
    data object Create : VibeDestination("create", "Create", Icons.Filled.AddBox, Icons.Outlined.AddBox)
    data object Notifications : VibeDestination("notifications", "Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    data object Profile : VibeDestination("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

private const val ROUTE_FOUNDER_ADMIN = "founder_admin"

@Composable
fun VibeMateApp() {
    var isDarkTheme by remember { mutableStateOf(true) }
    var showSplash by remember { mutableStateOf(true) }
    var pendingHandle by remember { mutableStateOf("") }
    var pendingDisplayName by remember { mutableStateOf("") }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    VIBEMATETheme(darkTheme = isDarkTheme) {
        if (showSplash) {
            SplashScreen(onFinished = { showSplash = false })
        } else {
            val user = currentUser
            if (user == null) {
                AuthScreen(
                    onAuthSuccess = { handle, displayName ->
                        pendingHandle = handle
                        pendingDisplayName = displayName
                        currentUser = Firebase.auth.currentUser
                    }
                )
            } else {
                val uid = user.uid
                key(uid) {
                    AuthenticatedVibeShell(
                        currentUserId = uid,
                        initialHandle = pendingHandle,
                        initialDisplayName = pendingDisplayName,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedVibeShell(
    currentUserId: String,
    initialHandle: String,
    initialDisplayName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: VibeViewModel = viewModel(
        key = currentUserId,
        factory = VibeViewModel.factory(
            currentUserId = currentUserId,
            initialHandle = initialHandle,
            initialDisplayName = initialDisplayName
        )
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val postsState by viewModel.postsState.collectAsStateWithLifecycle()
    val usersState by viewModel.usersState.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val adminRole by viewModel.adminRole.collectAsStateWithLifecycle()
    val likedPostIds by viewModel.likedPostIds.collectAsStateWithLifecycle()
    val bookmarkedPostIds by viewModel.bookmarkedPostIds.collectAsStateWithLifecycle()
    val followingIds by viewModel.followingIds.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
    val activePostComments by viewModel.activePostComments.collectAsStateWithLifecycle()
    val moderationReportsState by viewModel.moderationReportsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedExploreCategory by viewModel.selectedExploreCategory.collectAsStateWithLifecycle()
    val isActionBusy by viewModel.isActionBusy.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    var commentingPost by remember { mutableStateOf<VibePost?>(null) }
    var reportingPost by remember { mutableStateOf<VibePost?>(null) }

    val unreadNotificationsCount = (notificationsState as? UiState.Success)?.data?.count { !it.read } ?: 0

    val destinations = listOf(
        VibeDestination.Home,
        VibeDestination.Reels,
        VibeDestination.Explore,
        VibeDestination.Create,
        VibeDestination.Notifications,
        VibeDestination.Profile
    )

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun followPostAuthor(post: VibePost) {
        val users = (usersState as? UiState.Success)?.data ?: emptyList()
        val target = users.firstOrNull { it.userId == post.userId } ?: UserProfile(
            userId = post.userId,
            handle = post.authorHandle,
            displayName = post.authorName
        )
        viewModel.toggleFollow(target)
    }

    val showNavigation = currentRoute != ROUTE_FOUNDER_ADMIN

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showNavigation && !useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        destinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    if (dest == VibeDestination.Notifications && unreadNotificationsCount > 0) {
                                        BadgedBox(badge = {
                                            Badge(containerColor = VibeElectricBlue) {
                                                Text("$unreadNotificationsCount")
                                            }
                                        }) {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VibeElectricBlue,
                                    selectedTextColor = VibeElectricBlue,
                                    indicatorColor = VibePurple.copy(alpha = 0.22f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showNavigation && useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            Box(modifier = Modifier.padding(vertical = 12.dp)) {
                                VibePrismMark(size = 32.dp)
                            }
                        },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(Modifier.weight(0.2f))
                        destinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    if (dest == VibeDestination.Notifications && unreadNotificationsCount > 0) {
                                        BadgedBox(badge = {
                                            Badge(containerColor = VibeElectricBlue) {
                                                Text("$unreadNotificationsCount")
                                            }
                                        }) {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    }
                                },
                                label = { Text(dest.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = VibeElectricBlue,
                                    selectedTextColor = VibeElectricBlue,
                                    indicatorColor = VibePurple.copy(alpha = 0.22f)
                                ),
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = VibeDestination.Home.route
                    ) {
                        composable(VibeDestination.Home.route) {
                            HomeFeedScreen(
                                currentUserId = currentUserId,
                                currentUserProfile = currentUserProfile,
                                postsState = postsState,
                                usersState = usersState,
                                likedPostIds = likedPostIds,
                                bookmarkedPostIds = bookmarkedPostIds,
                                followingIds = followingIds,
                                isAdmin = adminRole != null,
                                isDarkTheme = isDarkTheme,
                                isBusy = isActionBusy,
                                onToggleTheme = onToggleTheme,
                                onOpenFounderAdmin = { navController.navigate(ROUTE_FOUNDER_ADMIN) },
                                onNavigateToCreate = { navigateToTab(VibeDestination.Create.route) },
                                onSyncLikes = { viewModel.syncLikeStateForPosts(it) },
                                onLikePost = { viewModel.toggleLike(it) },
                                onCommentPost = { post ->
                                    commentingPost = post
                                    viewModel.openCommentsForPost(post.id)
                                },
                                onBookmarkPost = { viewModel.toggleBookmark(it.id) },
                                onSharePost = { viewModel.recordShare(it.id) },
                                onFollowAuthor = { followPostAuthor(it) },
                                onReportPost = { reportingPost = it },
                                onSeedShowcase = { viewModel.seedLaunchShowcase() }
                            )
                        }

                        composable(VibeDestination.Reels.route) {
                            ReelsScreen(
                                currentUserId = currentUserId,
                                postsState = postsState,
                                likedPostIds = likedPostIds,
                                bookmarkedPostIds = bookmarkedPostIds,
                                followingIds = followingIds,
                                onLikePost = { viewModel.toggleLike(it) },
                                onCommentPost = { post ->
                                    commentingPost = post
                                    viewModel.openCommentsForPost(post.id)
                                },
                                onBookmarkPost = { viewModel.toggleBookmark(it.id) },
                                onSharePost = { viewModel.recordShare(it.id) },
                                onFollowAuthor = { followPostAuthor(it) },
                                onReportPost = { reportingPost = it },
                                onNavigateToCreate = { navigateToTab(VibeDestination.Create.route) }
                            )
                        }

                        composable(VibeDestination.Explore.route) {
                            ExploreScreen(
                                currentUserId = currentUserId,
                                searchQuery = searchQuery,
                                selectedCategory = selectedExploreCategory,
                                postsState = postsState,
                                usersState = usersState,
                                followingIds = followingIds,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onSelectCategory = { viewModel.selectExploreCategory(it) },
                                onToggleFollow = { viewModel.toggleFollow(it) },
                                onOpenReels = { navigateToTab(VibeDestination.Reels.route) }
                            )
                        }

                        composable(VibeDestination.Create.route) {
                            CreatePostScreen(
                                isBusy = isActionBusy,
                                onPublish = { postType, mediaType, mediaKey, caption, tags, audio ->
                                    viewModel.createPost(
                                        postType = postType,
                                        mediaType = mediaType,
                                        mediaKey = mediaKey,
                                        caption = caption,
                                        hashtagsRaw = tags,
                                        audioTitle = audio,
                                        onPublished = {
                                            navigateToTab(
                                                if (postType == "reel") VibeDestination.Reels.route
                                                else VibeDestination.Home.route
                                            )
                                        }
                                    )
                                }
                            )
                        }

                        composable(VibeDestination.Notifications.route) {
                            NotificationsScreen(
                                notificationsState = notificationsState,
                                onMarkRead = { viewModel.markNotificationRead(it) }
                            )
                        }

                        composable(VibeDestination.Profile.route) {
                            ProfileScreen(
                                currentUserId = currentUserId,
                                profile = currentUserProfile,
                                adminRole = adminRole,
                                postsState = postsState,
                                bookmarkedPostIds = bookmarkedPostIds,
                                isBusy = isActionBusy,
                                onUpdateProfile = { name, handle, bio, preset, onDone ->
                                    viewModel.updateProfile(name, handle, bio, preset, onDone)
                                },
                                onClaimFounderAccess = { viewModel.claimFounderAdminAccess() },
                                onOpenFounderAdmin = { navController.navigate(ROUTE_FOUNDER_ADMIN) },
                                onSignOut = {
                                    signOut(credentialManager = credentialManager, scope = scope)
                                }
                            )
                        }

                        composable(ROUTE_FOUNDER_ADMIN) {
                            FounderAdminScreen(
                                adminRole = adminRole,
                                postsState = postsState,
                                usersState = usersState,
                                reportsState = moderationReportsState,
                                isBusy = isActionBusy,
                                onBack = { navController.popBackStack() },
                                onResolveReport = { reportId, targetPostId, status, removePost, note ->
                                    viewModel.resolveReport(reportId, targetPostId, status, removePost, note)
                                },
                                onToggleUserVerified = { targetUid, verified ->
                                    viewModel.toggleUserVerified(targetUid, verified)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val activePost = commentingPost
    if (activePost != null) {
        CommentsBottomSheet(
            post = activePost,
            commentsState = activePostComments,
            isBusy = isActionBusy,
            onDismiss = {
                commentingPost = null
                viewModel.openCommentsForPost(null)
            },
            onSendComment = { text ->
                viewModel.addComment(activePost, text)
            }
        )
    }

    val targetReportPost = reportingPost
    if (targetReportPost != null) {
        ReportPostDialog(
            onDismiss = { reportingPost = null },
            onSubmit = { reason ->
                viewModel.submitReport(targetReportPost.id, reason) {
                    reportingPost = null
                }
            }
        )
    }
}
