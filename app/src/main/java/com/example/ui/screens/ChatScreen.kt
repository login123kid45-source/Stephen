package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.InteractionMode
import com.example.data.model.PersonalityArchetype
import com.example.data.model.RelationshipStage
import com.example.ui.MainViewModel
import com.example.ui.components.KaraAvatar
import com.example.ui.components.KaraTypingBubble
import com.example.ui.components.RelationshipBadge
import com.example.ui.theme.KaraAccentPink
import com.example.ui.theme.KaraPrimary
import com.example.ui.theme.KaraTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onNavigateToPersona: () -> Unit,
    onNavigateToCrib: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val isVoiceEnabled by viewModel.isVoiceEnabled.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val recognizedVoiceText by viewModel.recognizedVoiceText.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val isListening = speechState is com.example.ui.speech.SpeechState.Listening ||
            speechState is com.example.ui.speech.SpeechState.Initializing ||
            speechState is com.example.ui.speech.SpeechState.Processing

    // Keep live recognized text synced
    LaunchedEffect(recognizedVoiceText) {
        if (recognizedVoiceText.isNotBlank()) {
            inputText = recognizedVoiceText
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening { voiceResult ->
                if (voiceResult.isNotBlank()) {
                    inputText = voiceResult
                    viewModel.sendMessage(voiceResult)
                    inputText = ""
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val currentStage = try {
        RelationshipStage.valueOf(profile?.manualStageUnlocked ?: "CLOSE_FRIEND")
    } catch (e: Exception) {
        RelationshipStage.CLOSE_FRIEND
    }

    val currentArchetype = try {
        PersonalityArchetype.valueOf(profile?.selectedArchetype ?: "BRATTY")
    } catch (e: Exception) {
        PersonalityArchetype.BRATTY
    }

    val currentMode = try {
        InteractionMode.valueOf(profile?.currentMode ?: "REGULAR")
    } catch (e: Exception) {
        InteractionMode.REGULAR
    }

    val affinityPoints = profile?.affinityPoints ?: 350

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = remember(currentArchetype) {
        when (currentArchetype) {
            PersonalityArchetype.BRATTY -> listOf(
                "🌐 Search web: What's in the news today?",
                "🔬 What's the latest tech & AI news?",
                "Hey Kara, miss me? 😏",
                "Remind me to drink water in an hour",
                "I had a really rough day...",
                "*sits down opposite you and smirks*"
            )
            PersonalityArchetype.UNCENSORED_REAL -> listOf(
                "🌐 Real talk: What's happening in the news today?",
                "🔬 Break down the latest tech breakthroughs",
                "Schedule a reminder: Call mom at 6pm",
                "What do you honestly think about my goals?",
                "*leans back and takes a deep breath*"
            )
            PersonalityArchetype.PARENTAL -> listOf(
                "🌐 What's the latest health & wellness news?",
                "I haven't eaten yet today...",
                "Remind me to take my vitamins and sleep early",
                "Kara, can I get some comforting words?",
                "*hugs you gently*"
            )
            PersonalityArchetype.CHARMING_REGULAR -> listOf(
                "🌐 What are today's top world headlines?",
                "🔬 Tell me about exciting science & space discoveries",
                "Remind me to review today's schedule",
                "Tell me something thoughtful",
                "*steps into a quiet cozy library with you*"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_screen")
    ) {
        // Custom Top Bar with Kara Status
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    KaraAvatar(
                        size = 42.dp,
                        emotionEmoji = currentArchetype.icon,
                        isThinking = isSending
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Kara",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = currentArchetype.label,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                        Text(
                            text = if (isSending) "Kara is responding..." else currentArchetype.tagline,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = if (isSending) KaraTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }
            },
            actions = {
                // Relationship Badge
                RelationshipBadge(
                    stage = currentStage,
                    affinityPoints = affinityPoints,
                    onClick = onNavigateToPersona
                )

                // Visit Kara's Crib
                IconButton(
                    onClick = onNavigateToCrib,
                    modifier = Modifier.testTag("go_to_crib_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Visit Kara's Crib",
                        tint = KaraAccentPink
                    )
                }

                // Voice TTS Toggle
                IconButton(
                    onClick = { viewModel.toggleVoice() },
                    modifier = Modifier.testTag("voice_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isVoiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle Voice Readout",
                        tint = if (isVoiceEnabled) KaraPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Clear Chat Action
                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Chat History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        // Mode Ribbon (Regular vs Roleplay indicator + toggle)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val nextMode = if (currentMode == InteractionMode.REGULAR) InteractionMode.ROLEPLAY else InteractionMode.REGULAR
                            viewModel.toggleInteractionMode(nextMode)
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentMode.badge,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (currentMode == InteractionMode.ROLEPLAY) KaraAccentPink else KaraPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(Tap to switch)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = KaraTertiary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Web & News Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = KaraPrimary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (profile?.autoDetectRoleplay == true) "Auto-Detect" else "Manual",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    karaArchetype = currentArchetype
                )
            }

            if (isSending) {
                item {
                    KaraTypingBubble(karaName = "Kara")
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.sendMessage(prompt)
                        }
                ) {
                    Text(
                        text = prompt,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Active Speech Listening Banner
        AnimatedVisibility(
            visible = isListening,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = KaraAccentPink.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, KaraAccentPink.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(micPulseScale)
                            .background(KaraAccentPink, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (recognizedVoiceText.isNotBlank()) "Listening: \"$recognizedVoiceText\"" else "🎙️ Kara is listening... Speak to her now",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = KaraAccentPink
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        text = "Tap mic to finish",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Input Field, Mic Button and Send Button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Speaking..." else if (currentMode == InteractionMode.ROLEPLAY) "Action *in asterisks* or dialogue..." else "Talk, ask news, or say 'remind me to...'",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isListening) KaraAccentPink else KaraPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Microphone Speech Recognition Button
                IconButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopListening()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .scale(if (isListening) micPulseScale else 1f)
                        .background(
                            if (isListening) KaraAccentPink else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("microphone_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Speech Recognition Input",
                        tint = if (isListening) Color.White else KaraPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        val text = inputText
                        inputText = ""
                        viewModel.stopListening()
                        viewModel.sendMessage(text)
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (inputText.isNotBlank() && !isSending) KaraPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (inputText.isNotBlank() && !isSending) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    karaArchetype: PersonalityArchetype
) {
    val isUser = message.sender == "user"
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            KaraAvatar(
                size = 32.dp,
                emotionEmoji = ""
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) KaraPrimary else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    // Message content with stylized asterisks for roleplay
                    FormattedMessageText(
                        text = message.text,
                        isUser = isUser
                    )

                    // Kara extra indicators (Affinity, Emotion)
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (message.emotionalTag.isNotBlank()) {
                                Text(
                                    text = "• ${message.emotionalTag}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = KaraTertiary
                                    )
                                )
                            }
                            if (message.affinityGained > 0) {
                                Text(
                                    text = "+${message.affinityGained} Affinity",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KaraAccentPink
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun FormattedMessageText(text: String, isUser: Boolean) {
    // Style asterisks as italicized expressive text
    val annotated = remember(text, isUser) {
        buildAnnotatedString {
            var i = 0
            val pattern = Regex("\\*(.*?)\\*")
            val matches = pattern.findAll(text).toList()

            if (matches.isEmpty()) {
                append(text)
            } else {
                for (match in matches) {
                    val start = match.range.first
                    val end = match.range.last + 1
                    if (start > i) {
                        append(text.substring(i, start))
                    }
                    val roleplayChunk = match.groupValues[1]
                    withStyle(
                        style = SpanStyle(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = if (isUser) Color(0xFFFFE0B2) else Color(0xFFFF80AB)
                        )
                    ) {
                        append("*$roleplayChunk*")
                    }
                    i = end
                }
                if (i < text.length) {
                    append(text.substring(i))
                }
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
        )
    )
}
