package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user" or "kara"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRoleplay: Boolean = false,
    val emotionalTag: String = "", // e.g. "Happy", "Teasing", "Caring", "Supportive"
    val affinityGained: Int = 0
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Daily", // "Work", "Daily", "Health", "Emotional Well-being"
    val dueDateMillis: Long = System.currentTimeMillis() + 86400000L,
    val priority: String = "Medium", // "High", "Medium", "Low"
    val isCompleted: Boolean = false,
    val reminderTimeFormatted: String = "10:00 AM",
    val isKaraSuggested: Boolean = false
)

@Entity(tableName = "user_memories")
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "Preference", // "Personal", "Preference", "Goal", "Emotional Habit"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Stephen",
    val karaName: String = "Kara",
    val affinityPoints: Int = 450,
    val manualStageUnlocked: String = "CLOSE_FRIEND", // Current active stage
    val selectedArchetype: String = "BRATTY", // BRATTY, UNCENSORED_REAL, PARENTAL, CHARMING_REGULAR
    val selectedTone: String = "PLAYFUL",
    val currentMode: String = "REGULAR", // REGULAR, ROLEPLAY
    val autoDetectRoleplay: Boolean = true,
    val wardrobeStyle: String = "GOTHIC", // GOTHIC, URBAN_STREETWEAR, TOMBOY_SPORTY, LOUNGE_CASUAL
    val roomTheme: String = "NEON_PENTHOUSE", // NEON_PENTHOUSE, COZY_LOFT, GOTHIC_SANCTUARY, SUNSET_STUDIO
    val roomLighting: String = "CYBER_NEON", // CYBER_NEON, GOLDEN_HOUR, CANDLELIGHT, COOL_DAYLIGHT, MIDNIGHT_VELVET
    val customApiKey: String = ""
)
