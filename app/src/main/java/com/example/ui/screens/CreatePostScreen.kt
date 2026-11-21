package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.VibeMediaSurface
import com.example.ui.components.VibePrimaryGradientButton
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibePurple

@Composable
fun CreatePostScreen(
    isBusy: Boolean,
    onPublish: (
        postType: String,
        mediaType: String,
        mediaKey: String,
        caption: String,
        hashtagsRaw: String,
        audioTitle: String
    ) -> Unit
) {
    val context = LocalContext.current
    var postType by remember { mutableStateOf("post") }
    var mediaType by remember { mutableStateOf("photo") }
    var selectedMediaKey by remember { mutableStateOf("studio") }
    var caption by remember { mutableStateOf("") }
    var hashtagsRaw by remember { mutableStateOf("vibemate, creative") }
    var audioTitle by remember { mutableStateOf("") }

    // Zero-permission Android Photo Picker per Google Play policy
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: ""
            mediaType = if (mime.startsWith("video")) "video" else "photo"
            selectedMediaKey = uri.toString()
        }
    }

    val presetVisuals = listOf(
        "studio" to "Neon Studio",
        "horizon" to "Skyline Dusk",
        "reel_motion" to "Kinetic Stage",
        "splash" to "Prism Glass"
    )

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Create Vibe",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Share a photo post or vertical reel with the global VIBEMATE community.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(16.dp))

            // Format Switcher: Post vs Reel
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("post" to "Feed Post", "reel" to "Vertical Reel").forEach { (key, label) ->
                    val active = postType == key
                    Surface(
                        onClick = {
                            postType = key
                            if (key == "reel") mediaType = "video"
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (active) VibePurple else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (active) VibeElectricBlue else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (key == "reel") Icons.Filled.Videocam else Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                tint = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Live Visual Preview
            VibeMediaSurface(
                mediaKey = selectedMediaKey,
                mediaType = mediaType,
                contentDescription = "New vibe preview",
                aspectRatio = if (postType == "reel") 9f / 11f else 4f / 3f
            )

            Spacer(Modifier.height(12.dp))

            // Zero-permission Photo/Video Picker
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pick_media_button")
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = VibeElectricBlue)
                Spacer(Modifier.width(8.dp))
                Text("Select Photo or Video from Device")
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Or choose an editorial studio canvas:",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presetVisuals.forEach { (key, title) ->
                    val chosen = selectedMediaKey == key
                    Surface(
                        onClick = { selectedMediaKey = key },
                        shape = RoundedCornerShape(12.dp),
                        color = if (chosen) VibePurple.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (chosen) VibeElectricBlue else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = caption,
                onValueChange = { if (it.length <= 500) caption = it },
                label = { Text("Caption") },
                placeholder = { Text("Share the story behind this vibe...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_caption_input")
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = hashtagsRaw,
                onValueChange = { hashtagsRaw = it },
                label = { Text("Hashtags (comma or space separated)") },
                leadingIcon = { Icon(Icons.Filled.Tag, contentDescription = null, tint = VibeElectricBlue) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_hashtags_input")
            )

            if (postType == "reel") {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = audioTitle,
                    onValueChange = { if (it.length <= 100) audioTitle = it },
                    label = { Text("Audio Track (Optional)") },
                    placeholder = { Text("e.g. Prism Pulse • Original Audio") },
                    leadingIcon = { Icon(Icons.Filled.MusicNote, contentDescription = null, tint = VibePurple) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(22.dp))

            VibePrimaryGradientButton(
                text = if (postType == "reel") "Publish Reel" else "Publish Post",
                loading = isBusy,
                enabled = caption.isNotBlank() || selectedMediaKey.isNotBlank(),
                testTag = "publish_vibe_button",
                onClick = {
                    onPublish(
                        postType,
                        mediaType,
                        selectedMediaKey,
                        caption.ifBlank { "Fresh drop on VIBEMATE." },
                        hashtagsRaw,
                        audioTitle
                    )
                    caption = ""
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
