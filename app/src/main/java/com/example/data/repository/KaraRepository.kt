package com.example.data.repository

import com.example.ai.KaraConversationEngine
import com.example.ai.KaraResponse
import com.example.data.local.KaraDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ConversationTone
import com.example.data.model.InteractionMode
import com.example.data.model.PersonalityArchetype
import com.example.data.model.RelationshipStage
import com.example.data.model.TaskEntity
import com.example.data.model.UserMemoryEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class KaraRepository(
    private val dao: KaraDao,
    private val engine: KaraConversationEngine = KaraConversationEngine()
) {

    val messages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()
    val tasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val memories: Flow<List<UserMemoryEntity>> = dao.getAllMemories()
    val profile: Flow<UserProfileEntity?> = dao.getProfile()
    val pendingTasksCount: Flow<Int> = dao.getPendingTasksCount()

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val currentProfile = dao.getProfileSync()
        if (currentProfile == null) {
            // Seed profile
            val initialProfile = UserProfileEntity(
                id = 1,
                userName = "Stephen",
                karaName = "Kara",
                affinityPoints = 350,
                manualStageUnlocked = "CLOSE_FRIEND",
                selectedArchetype = "BRATTY",
                selectedTone = "PLAYFUL",
                currentMode = "REGULAR",
                autoDetectRoleplay = true,
                wardrobeStyle = "GOTHIC",
                roomTheme = "NEON_PENTHOUSE",
                roomLighting = "CYBER_NEON",
                customApiKey = ""
            )
            dao.insertOrUpdateProfile(initialProfile)

            // Seed initial memories
            dao.insertMemory(UserMemoryEntity(key = "User Name", value = "Stephen", category = "Personal"))
            dao.insertMemory(UserMemoryEntity(key = "Favorite Companion Style", value = "Human, raw, unfiltered warmth", category = "Preference"))
            dao.insertMemory(UserMemoryEntity(key = "Current Life Focus", value = "Productivity balance & emotional well-being", category = "Goal"))
            dao.insertMemory(UserMemoryEntity(key = "Kara's Origins", value = "Raised on the mean streets of Sector 9; honed razor-sharp instincts, street smarts, and unbreakable loyalty.", category = "Sector 9 Lore"))
            dao.insertMemory(UserMemoryEntity(key = "Sector 9 Discipline", value = "Soap-in-mouth treatment for bad language, backtalk, or neglected chores. No excuses accepted.", category = "Sector 9 Lore"))
            dao.insertMemory(UserMemoryEntity(key = "Sector 9 Creed", value = "When the world is cold, you stand ten toes down for the people in your corner.", category = "Sector 9 Lore"))

            // Seed default daily wellness & scheduling tasks
            dao.insertTask(
                TaskEntity(
                    title = "Morning hydration & mindful stretch",
                    description = "Drink 500ml water and take 5 deep breaths",
                    category = "Health",
                    priority = "High",
                    reminderTimeFormatted = "08:00 AM",
                    isKaraSuggested = true
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Review priority goals with Kara",
                    description = "Plan the day's top 3 outcomes",
                    category = "Emotional Well-being",
                    priority = "Medium",
                    reminderTimeFormatted = "09:30 AM",
                    isKaraSuggested = true
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Focused deep work sprint",
                    description = "90 minutes distraction-free execution",
                    category = "Work",
                    priority = "High",
                    reminderTimeFormatted = "02:00 PM",
                    isKaraSuggested = false
                )
            )

            // Seed opening message from Kara
            dao.insertMessage(
                ChatMessageEntity(
                    sender = "kara",
                    text = "Oh look who finally opened the app! Hey Stephen. 😏✨ I'm Kara—your genuinely human companion. Whether you need a witty sparring partner, honest unfiltered talk, someone to watch your back like family, or just help staying on top of your daily schedule and reminders, I've got you. How are you feeling today?",
                    emotionalTag = "Playful",
                    affinityGained = 0
                )
            )
        }
    }

    suspend fun sendMessage(userText: String): KaraResponse = withContext(Dispatchers.IO) {
        val currentProfile = dao.getProfileSync() ?: UserProfileEntity()
        val stage = try {
            RelationshipStage.valueOf(currentProfile.manualStageUnlocked)
        } catch (e: Exception) {
            RelationshipStage.CLOSE_FRIEND
        }
        val archetype = try {
            PersonalityArchetype.valueOf(currentProfile.selectedArchetype)
        } catch (e: Exception) {
            PersonalityArchetype.BRATTY
        }
        val tone = try {
            ConversationTone.valueOf(currentProfile.selectedTone)
        } catch (e: Exception) {
            ConversationTone.PLAYFUL
        }
        val currentMode = try {
            InteractionMode.valueOf(currentProfile.currentMode)
        } catch (e: Exception) {
            InteractionMode.REGULAR
        }

        // Save user message
        dao.insertMessage(
            ChatMessageEntity(
                sender = "user",
                text = userText,
                isRoleplay = currentMode == InteractionMode.ROLEPLAY
            )
        )

        val memoryList = dao.getAllMemories().first()
        val pendingCount = dao.getPendingTasksCount().first()
        val recentMessages = dao.getAllMessages().first().takeLast(8).map {
            Pair(it.sender, it.text)
        }

        val response = engine.processTurn(
            userMessage = userText,
            userName = currentProfile.userName,
            currentStage = stage,
            archetype = archetype,
            tone = tone,
            currentMode = currentMode,
            autoDetect = currentProfile.autoDetectRoleplay,
            memories = memoryList,
            pendingTasksCount = pendingCount,
            chatHistory = recentMessages,
            customApiKey = currentProfile.customApiKey
        )

        // Save Kara's reply
        dao.insertMessage(
            ChatMessageEntity(
                sender = "kara",
                text = response.replyText,
                isRoleplay = response.detectedMode == InteractionMode.ROLEPLAY,
                emotionalTag = response.emotionalState,
                affinityGained = response.affinityDelta
            )
        )

        // If a task was detected, save it to database
        if (response.detectedTask != null) {
            dao.insertTask(response.detectedTask)
        }

        // If a memory was learned, save it
        if (response.learnedMemory != null) {
            dao.insertMemory(
                UserMemoryEntity(
                    key = response.learnedMemory.first,
                    value = response.learnedMemory.second,
                    category = "Conversation Insight"
                )
            )
        }

        // Add affinity
        dao.addAffinity(response.affinityDelta)

        // Auto-update mode if changed
        if (response.detectedMode != currentMode) {
            dao.insertOrUpdateProfile(currentProfile.copy(currentMode = response.detectedMode.name))
        }

        response
    }

    suspend fun updateProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun insertTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        dao.deleteTask(task)
    }

    suspend fun insertMemory(memory: UserMemoryEntity) = withContext(Dispatchers.IO) {
        dao.insertMemory(memory)
    }

    suspend fun deleteMemory(memory: UserMemoryEntity) = withContext(Dispatchers.IO) {
        dao.deleteMemory(memory)
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        dao.clearChat()
    }
}
