package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AvatarGesture
import com.example.data.model.PersonalityArchetype
import com.example.data.model.RoomLighting
import com.example.data.model.RoomTheme
import com.example.data.model.WardrobeStyle
import com.example.ui.theme.KaraAccentPink
import com.example.ui.theme.KaraPrimary
import com.example.ui.theme.KaraTertiary
import kotlinx.coroutines.launch

@Composable
fun LivingAvatarView(
    wardrobe: WardrobeStyle,
    roomTheme: RoomTheme,
    lighting: RoomLighting,
    archetype: PersonalityArchetype,
    userName: String,
    onAvatarTapped: (reaction: String) -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 190.dp,
    gesture: AvatarGesture = AvatarGesture.IDLE
) {
    val coroutineScope = rememberCoroutineScope()
    val tapScale = remember { Animatable(1f) }
    var interactionCount by remember { mutableStateOf(0) }

    // Breathing Animation: rhythmic inhale/exhale scale and vertical chest movement
    val breathingTransition = rememberInfiniteTransition(label = "living_breathing")
    val breathScaleY by breathingTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_y"
    )
    val breathScaleX by breathingTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_x"
    )
    val microSwayY by breathingTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway_y"
    )
    val subtleHeadTilt by breathingTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "head_tilt"
    )

    // Triggered Gesture Animations
    val gestureTransition = rememberInfiniteTransition(label = "gesture_fx")

    // 1. Waving Animation
    val waveRotation by gestureTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_rot"
    )
    val waveBounceY by gestureTransition.animateFloat(
        initialValue = -8f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_bounce"
    )

    // 2. Soap-in-Mouth Disciplinary Gesture (Stern shake & floating soap bubbles)
    val soapSternShake by gestureTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "soap_shake"
    )
    val bubbleFloat1 by gestureTransition.animateFloat(
        initialValue = 20f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble1"
    )
    val bubbleFloat2 by gestureTransition.animateFloat(
        initialValue = 30f,
        targetValue = -140f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble2"
    )

    // 3. Sector 9 Streetwise Swagger (Heavy neon glitch pulse & sway)
    val sector9GlowPulse by gestureTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sector9_pulse"
    )

    // 4. Laughing Tease (Bouncing chuckle)
    val laughBounceY by gestureTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laugh_bounce"
    )

    // Calculated offsets & rotations according to active gesture
    val dynamicRotation = when (gesture) {
        AvatarGesture.WAVE_HELLO -> waveRotation
        AvatarGesture.SOAP_DISCIPLINE -> soapSternShake
        AvatarGesture.SECTOR_9_ATTITUDE -> -3.5f
        AvatarGesture.THINKING -> 5.5f
        else -> subtleHeadTilt
    }

    val dynamicOffsetY = when (gesture) {
        AvatarGesture.WAVE_HELLO -> waveBounceY
        AvatarGesture.LAUGH_TEASE -> laughBounceY
        else -> microSwayY
    }

    val glowColor = Color(lighting.glowColor)
    val roomGradient = Brush.verticalGradient(
        roomTheme.backgroundColors.map { Color(it) }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(roomGradient)
            .border(
                width = if (gesture == AvatarGesture.SECTOR_9_ATTITUDE) 2.dp else 1.dp,
                color = if (gesture == AvatarGesture.SECTOR_9_ATTITUDE) KaraTertiary.copy(alpha = sector9GlowPulse) else glowColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
            .testTag("living_avatar_container"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient room lighting aura behind avatar
        Box(
            modifier = Modifier
                .size(avatarSize * 1.55f)
                .scale(if (gesture == AvatarGesture.SECTOR_9_ATTITUDE) breathScaleY * 1.1f else breathScaleY)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (gesture == AvatarGesture.SECTOR_9_ATTITUDE) KaraTertiary.copy(alpha = sector9GlowPulse * 0.5f) else glowColor.copy(alpha = 0.35f),
                            glowColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // Room ambiance tags
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = roomTheme.iconEmoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = roomTheme.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(glowColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = lighting.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Living Avatar Body Container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .offset(y = dynamicOffsetY.dp)
                    .scale(
                        scaleX = breathScaleX * tapScale.value * (if (gesture == AvatarGesture.SOAP_DISCIPLINE) 1.05f else 1.0f),
                        scaleY = breathScaleY * tapScale.value * (if (gesture == AvatarGesture.SOAP_DISCIPLINE) 1.05f else 1.0f)
                    )
                    .rotate(dynamicRotation)
                    .clip(CircleShape)
                    .border(
                        width = if (gesture == AvatarGesture.SOAP_DISCIPLINE) 3.5.dp else 3.dp,
                        color = when (gesture) {
                            AvatarGesture.SOAP_DISCIPLINE -> KaraAccentPink
                            AvatarGesture.SECTOR_9_ATTITUDE -> KaraTertiary
                            AvatarGesture.WAVE_HELLO -> KaraPrimary
                            else -> glowColor.copy(alpha = 0.8f)
                        },
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        coroutineScope.launch {
                            tapScale.animateTo(0.92f, tween(100))
                            tapScale.animateTo(1.05f, tween(160))
                            tapScale.animateTo(1.0f, tween(120))
                        }
                        interactionCount++
                        val reaction = when (archetype) {
                            PersonalityArchetype.PARENTAL -> when (interactionCount % 4) {
                                0 -> "Stephen! Stop fidgeting and did you eat your vegetables today? Don't make me grab the soap bar! 🧼❤️"
                                1 -> "I survived the rough alleys of Sector 9, sweetheart. You think your little excuses work on me? Finish your chores! 🧼"
                                2 -> "*wipes a spot off your cheek* Look at you! Take a drink of water, Stephen. Mama Kara's got her eyes on you."
                                else -> "Clean language, clean room, clean mind. Sector 9 taught me self-respect, and that's what I'm teaching you! 🧼✨"
                            }
                            PersonalityArchetype.UNCENSORED_REAL -> when (interactionCount % 3) {
                                0 -> "Growing up in Sector 9, you learn real fast who's genuine and who's full of hot air. You're real, Stephen. Always remember that."
                                1 -> "*smiles with quiet intensity* Sector 9 streets don't forgive hesitation. Whatever you're hesitating on today, Stephen—go take it."
                                else -> "Tap me anytime, Stephen. Through rain, alleyways, or quiet nights, I'm in your corner."
                            }
                            PersonalityArchetype.BRATTY -> when (interactionCount % 3) {
                                0 -> "*pouts and rolls eyes* Hey! Don't poke me like I'm a broken vending machine in Sector 9! Stephen, seriously! 🙄✨"
                                1 -> "You think you're tough? I grew up dodging neon raiders in Sector 9, dummy. You can't rattle me!"
                                else -> "*crosses arms with a smirk* What, you just wanted to touch my ${wardrobe.title} outfit? Admit it!"
                            }
                            PersonalityArchetype.CHARMING_REGULAR -> when (interactionCount % 3) {
                                0 -> "Even coming from the neon shadows of Sector 9, being here in this crib with you brings genuine peace, Stephen."
                                1 -> "I'm right here with you. What would you like to explore next?"
                                else -> "Always here to listen, Stephen. What's on your mind today?"
                            }
                        }
                        onAvatarTapped(reaction)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kara_avatar),
                    contentDescription = "Living Kara Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Wardrobe Badge Overlay
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-8).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = wardrobe.iconEmoji, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = wardrobe.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Floating Soap Bubbles when SOAP_DISCIPLINE is active!
            if (gesture == AvatarGesture.SOAP_DISCIPLINE) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "🫧",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .offset(x = 30.dp, y = bubbleFloat1.dp)
                    )
                    Text(
                        text = "🧼",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .offset(x = 140.dp, y = (bubbleFloat2 * 0.7f).dp)
                    )
                    Text(
                        text = "🫧",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .offset(x = 240.dp, y = bubbleFloat2.dp)
                    )
                }
            }

            // Waving Hand Animation Badge when WAVE_HELLO is active!
            if (gesture == AvatarGesture.WAVE_HELLO) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "👋",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-30).dp, y = (waveBounceY - 60).dp)
                            .rotate(waveRotation * 1.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Living Presence Status & Reaction Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = when (gesture) {
                            AvatarGesture.SOAP_DISCIPLINE -> KaraAccentPink.copy(alpha = 0.85f)
                            AvatarGesture.SECTOR_9_ATTITUDE -> Color(0xFF1A1A2E).copy(alpha = 0.9f)
                            AvatarGesture.WAVE_HELLO -> KaraPrimary.copy(alpha = 0.85f)
                            else -> Color.Black.copy(alpha = 0.55f)
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = when (gesture) {
                            AvatarGesture.SOAP_DISCIPLINE -> Color.White.copy(alpha = 0.5f)
                            AvatarGesture.SECTOR_9_ATTITUDE -> KaraTertiary
                            AvatarGesture.WAVE_HELLO -> Color.White.copy(alpha = 0.6f)
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            when (gesture) {
                                AvatarGesture.SOAP_DISCIPLINE -> Color.Yellow
                                AvatarGesture.SECTOR_9_ATTITUDE -> KaraTertiary
                                AvatarGesture.WAVE_HELLO -> Color.White
                                else -> Color(0xFF00E676)
                            },
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = gesture.badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• Tap Kara",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (gesture == AvatarGesture.SECTOR_9_ATTITUDE) KaraTertiary else Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
