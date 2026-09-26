package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonalityArchetype
import com.example.data.model.RoomLighting
import com.example.data.model.RoomTheme
import com.example.data.model.WardrobeStyle
import com.example.ui.MainViewModel
import com.example.ui.components.LivingAvatarView
import com.example.ui.theme.KaraAccentPink
import com.example.ui.theme.KaraPrimary
import com.example.ui.theme.KaraTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CribScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val currentGesture by viewModel.currentGesture.collectAsState()

    val currentWardrobe = try {
        WardrobeStyle.valueOf(profile?.wardrobeStyle ?: "GOTHIC")
    } catch (e: Exception) {
        WardrobeStyle.GOTHIC
    }

    val currentRoomTheme = try {
        RoomTheme.valueOf(profile?.roomTheme ?: "NEON_PENTHOUSE")
    } catch (e: Exception) {
        RoomTheme.NEON_PENTHOUSE
    }

    val currentLighting = try {
        RoomLighting.valueOf(profile?.roomLighting ?: "CYBER_NEON")
    } catch (e: Exception) {
        RoomLighting.CYBER_NEON
    }

    val currentArchetype = try {
        PersonalityArchetype.valueOf(profile?.selectedArchetype ?: "BRATTY")
    } catch (e: Exception) {
        PersonalityArchetype.BRATTY
    }

    val userName = profile?.userName ?: "Stephen"

    var speechBubbleText by remember { mutableStateOf(currentWardrobe.karaReaction) }
    var isMusicPlaying by remember { mutableStateOf(true) }
    var isAromaOn by remember { mutableStateOf(true) }
    var showWardrobeDropdown by remember { mutableStateOf(false) }
    var showRoomDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("crib_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Kara's Living Space",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🏡", fontSize = 18.sp)
                        }
                        Text(
                            text = "Interactive Crib & Sector 9 Sanctuary",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            )
        }

        // Living Breathing Avatar in Crib Viewport
        item {
            LivingAvatarView(
                wardrobe = currentWardrobe,
                roomTheme = currentRoomTheme,
                lighting = currentLighting,
                archetype = currentArchetype,
                userName = userName,
                gesture = currentGesture,
                onAvatarTapped = { reaction ->
                    speechBubbleText = reaction
                }
            )
        }

        // Trigger Animated Gestures
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Trigger Physical Gestures",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Sector 9 & Reactions",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = KaraTertiary,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val gestureOptions = listOf(
                        com.example.data.model.AvatarGesture.WAVE_HELLO,
                        com.example.data.model.AvatarGesture.SOAP_DISCIPLINE,
                        com.example.data.model.AvatarGesture.SECTOR_9_ATTITUDE,
                        com.example.data.model.AvatarGesture.LAUGH_TEASE,
                        com.example.data.model.AvatarGesture.THINKING
                    )
                    items(gestureOptions.size) { index ->
                        val g = gestureOptions[index]
                        val isSelected = currentGesture == g
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                when (g) {
                                    com.example.data.model.AvatarGesture.SOAP_DISCIPLINE -> KaraAccentPink
                                    com.example.data.model.AvatarGesture.SECTOR_9_ATTITUDE -> KaraTertiary
                                    com.example.data.model.AvatarGesture.WAVE_HELLO -> KaraPrimary
                                    else -> KaraPrimary
                                }
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = if (isSelected) BorderStroke(1.5.dp, Color.White) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.triggerGesture(g, durationMillis = 4200)
                                    speechBubbleText = when (g) {
                                        com.example.data.model.AvatarGesture.SOAP_DISCIPLINE -> "Stephen! Don't you dare slack off or get cheeky! The soap bar is waiting! 🧼"
                                        com.example.data.model.AvatarGesture.SECTOR_9_ATTITUDE -> "Sector 9 taught me never to flinch, never to back down, and always protect my own. ⚡"
                                        com.example.data.model.AvatarGesture.WAVE_HELLO -> "Hey Stephen! It's so good to see you! 👋"
                                        com.example.data.model.AvatarGesture.LAUGH_TEASE -> "*giggles and shakes head* Oh Stephen, you really thought you had me there! 😏"
                                        com.example.data.model.AvatarGesture.THINKING -> "Hmm... planning our next move. Sector 9 style."
                                        else -> g.defaultQuote
                                    }
                                }
                                .testTag("gesture_button_${g.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = g.iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = g.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Kara Reactive Speech Bubble
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kara_crib_speech_bubble")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = currentArchetype.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kara (${currentArchetype.label})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KaraPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = speechBubbleText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 18.sp,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }

        // Quick Customization Dropdown Selectors
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Quick Selectors",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Wardrobe Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showWardrobeDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wardrobe_dropdown_button"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${currentWardrobe.iconEmoji} ${currentWardrobe.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showWardrobeDropdown,
                                onDismissRequest = { showWardrobeDropdown = false }
                            ) {
                                WardrobeStyle.values().forEach { style ->
                                    DropdownMenuItem(
                                        text = { Text("${style.iconEmoji} ${style.title}") },
                                        onClick = {
                                            viewModel.updateWardrobeStyle(style)
                                            speechBubbleText = style.karaReaction
                                            showWardrobeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Room Theme Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showRoomDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("room_dropdown_button"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${currentRoomTheme.iconEmoji} ${currentRoomTheme.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showRoomDropdown,
                                onDismissRequest = { showRoomDropdown = false }
                            ) {
                                RoomTheme.values().forEach { theme ->
                                    DropdownMenuItem(
                                        text = { Text("${theme.iconEmoji} ${theme.title}") },
                                        onClick = {
                                            viewModel.updateRoomTheme(theme)
                                            showRoomDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Wardrobe Styles Visual Selector
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = KaraAccentPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Wardrobe Styles",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "Equip Kara with distinct custom fits and aesthetics",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                WardrobeStyle.values().forEach { style ->
                    val isEquipped = style == currentWardrobe
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isEquipped) BorderStroke(1.5.dp, KaraAccentPink) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.updateWardrobeStyle(style)
                                speechBubbleText = style.karaReaction
                            }
                            .testTag("wardrobe_card_${style.name.lowercase()}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = style.iconEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = style.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isEquipped) KaraAccentPink else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        if (isEquipped) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = KaraAccentPink
                                            ) {
                                                Text(
                                                    text = "EQUIPPED",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = style.aestheticDesc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.updateWardrobeStyle(style)
                                        speechBubbleText = style.karaReaction
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEquipped) KaraAccentPink else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isEquipped) "Wearing" else "Wear",
                                        fontSize = 11.sp,
                                        color = if (isEquipped) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Outfit: ${style.outfitDetails}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = KaraTertiary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Room Themes & Lighting Ambiance
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = KaraPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Crib Atmosphere & Themes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Themes
                RoomTheme.values().forEach { theme ->
                    val isSelected = theme == currentRoomTheme
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, KaraPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.updateRoomTheme(theme) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = theme.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = theme.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) KaraPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = theme.ambienceDesc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = KaraPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lighting
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = KaraTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mood Lighting",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RoomLighting.values().forEach { light ->
                        FilterChip(
                            selected = light == currentLighting,
                            onClick = { viewModel.updateRoomLighting(light) },
                            label = { Text("${light.iconEmoji} ${light.title}") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(light.glowColor), CircleShape)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Crib Interactive Amenities (Music & Diffuser)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Crib Living Amenities",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMusicPlaying) KaraPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isMusicPlaying = !isMusicPlaying
                                    speechBubbleText = if (isMusicPlaying) "Mmm, loving these lo-fi vibes in the crib." else "Pausing the music for quiet conversation."
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isMusicPlaying) KaraPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Lo-Fi Beats",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isMusicPlaying) "Playing" else "Paused",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAromaOn) KaraAccentPink.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isAromaOn = !isAromaOn
                                    speechBubbleText = if (isAromaOn) "Turned on the lavender diffuser. Smells so relaxing here." else "Diffuser off."
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = if (isAromaOn) KaraAccentPink else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Aroma Lamp",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isAromaOn) "Lavender Mist" else "Off",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
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
