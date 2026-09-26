package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RelationshipStage
import com.example.ui.theme.KaraAccentPink
import com.example.ui.theme.KaraPrimary

@Composable
fun RelationshipBadge(
    stage: RelationshipStage,
    affinityPoints: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        modifier = modifier
            .testTag("relationship_badge")
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stage.iconEmoji,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = stage.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "$affinityPoints Affinity",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun RelationshipCard(
    stage: RelationshipStage,
    affinityPoints: Int,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextStage = when (stage) {
        RelationshipStage.STRANGER -> RelationshipStage.ACQUAINTANCE
        RelationshipStage.ACQUAINTANCE -> RelationshipStage.CLOSE_FRIEND
        RelationshipStage.CLOSE_FRIEND -> RelationshipStage.CONFIDANT_PARTNER
        RelationshipStage.CONFIDANT_PARTNER -> RelationshipStage.MARRIAGE
        RelationshipStage.MARRIAGE -> null
        RelationshipStage.PARENT_GUARDIAN -> null
    }

    val progress = if (nextStage != null) {
        val range = (nextStage.minAffinity - stage.minAffinity).toFloat()
        val current = (affinityPoints - stage.minAffinity).coerceAtLeast(0).toFloat()
        (current / range).coerceIn(0f, 1f)
    } else {
        1.0f
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .testTag("relationship_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stage.iconEmoji, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Relationship: ${stage.title}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stage.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KaraPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { onManageClick() }
                ) {
                    Text(
                        text = "Customize",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = KaraPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Affinity: $affinityPoints XP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (nextStage != null) "Next: ${nextStage.title} (${nextStage.minAffinity} XP)" else "Max Bond Unlocked!",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = KaraAccentPink,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✨ Active Perk: ${stage.unlockedPerk}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
