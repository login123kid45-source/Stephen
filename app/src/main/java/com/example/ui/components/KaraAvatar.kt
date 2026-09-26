package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.KaraAccentPink
import com.example.ui.theme.KaraPrimary
import com.example.ui.theme.KaraTertiary

@Composable
fun KaraAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    emotionEmoji: String = "✨",
    isThinking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isThinking) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isThinking) 700 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val borderGlow = Brush.sweepGradient(
        listOf(
            KaraPrimary,
            KaraAccentPink,
            KaraTertiary,
            KaraPrimary
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("kara_avatar"),
        contentAlignment = Alignment.Center
    ) {
        // Glowing animated ring
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulseScale)
                .border(2.dp, borderGlow, CircleShape)
        )

        // Avatar Image
        Image(
            painter = painterResource(id = R.drawable.kara_avatar),
            contentDescription = "Kara AI Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size - 4.dp)
                .clip(CircleShape)
        )

        // Emotion Badge
        if (emotionEmoji.isNotBlank()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(size * 0.38f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = emotionEmoji,
                        fontSize = (size.value * 0.22f).sp
                    )
                }
            }
        }
    }
}
