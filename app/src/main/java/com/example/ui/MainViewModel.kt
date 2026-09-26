package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ConversationTone
import com.example.data.model.InteractionMode
import com.example.data.model.PersonalityArchetype
import com.example.data.model.RelationshipStage
import com.example.data.model.TaskEntity
import com.example.data.model.UserMemoryEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.KaraRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository: KaraRepository
    private val speechHelper = com.example.ui.speech.SpeechRecognizerHelper(application)
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    val speechState: StateFlow<com.example.ui.speech.SpeechState> = speechHelper.speechState
    val recognizedVoiceText: StateFlow<String> = speechHelper.currentText

    private val _currentGesture = MutableStateFlow(com.example.data.model.AvatarGesture.IDLE)
    val currentGesture: StateFlow<com.example.data.model.AvatarGesture> = _currentGesture.asStateFlow()
    private var gestureJob: kotlinx.coroutines.Job? = null

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _isVoiceEnabled = MutableStateFlow(false)
    val isVoiceEnabled: StateFlow<Boolean> = _isVoiceEnabled.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val messages: StateFlow<List<ChatMessageEntity>>
    val tasks: StateFlow<List<TaskEntity>>
    val memories: StateFlow<List<UserMemoryEntity>>
    val profile: StateFlow<UserProfileEntity?>
    val pendingTasksCount: StateFlow<Int>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = KaraRepository(db.karaDao())

        messages = repository.messages.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        tasks = repository.tasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        memories = repository.memories.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        profile = repository.profile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
        pendingTasksCount = repository.pendingTasksCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        viewModelScope.launch {
            repository.initializeIfNeeded()
            // Wave cheerfully when the app opens!
            triggerGesture(com.example.data.model.AvatarGesture.WAVE_HELLO, durationMillis = 3800)
        }

        try {
            tts = TextToSpeech(application, this)
        } catch (e: Exception) {
            // TTS unavailable
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsInitialized = true
        }
    }

    fun toggleVoice() {
        val next = !_isVoiceEnabled.value
        _isVoiceEnabled.value = next
        if (!next) {
            tts?.stop()
        }
    }

    fun speakText(text: String) {
        if (_isVoiceEnabled.value && isTtsInitialized) {
            // Strip asterisks and emojis for cleaner speech
            val clean = text.replace(Regex("\\*.*?\\*"), "")
                .replace(Regex("[^\\p{L}\\p{Nd}\\p{P}\\s]"), "")
                .trim()
            if (clean.isNotBlank()) {
                tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "kara_reply")
            }
        }
    }

    fun triggerGesture(gesture: com.example.data.model.AvatarGesture, durationMillis: Long = 3500) {
        gestureJob?.cancel()
        gestureJob = viewModelScope.launch {
            _currentGesture.value = gesture
            kotlinx.coroutines.delay(durationMillis)
            _currentGesture.value = com.example.data.model.AvatarGesture.IDLE
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isSending.value) return

        val lowerUser = trimmed.lowercase(Locale.ROOT)
        // Trigger pre-gesture if user prompts specific topics
        when {
            lowerUser.contains("soap") || lowerUser.contains("mouth") || lowerUser.contains("chore") || lowerUser.contains("wash") || lowerUser.contains("discipline") -> {
                triggerGesture(com.example.data.model.AvatarGesture.SOAP_DISCIPLINE, 4500)
            }
            lowerUser.contains("sector 9") || lowerUser.contains("street") || lowerUser.contains("hustle") || lowerUser.contains("grit") || lowerUser.contains("where are you from") -> {
                triggerGesture(com.example.data.model.AvatarGesture.SECTOR_9_ATTITUDE, 4500)
            }
            lowerUser.contains("hello") || lowerUser.contains("hi kara") || lowerUser.contains("hey kara") || lowerUser.contains("wave") -> {
                triggerGesture(com.example.data.model.AvatarGesture.WAVE_HELLO, 3500)
            }
            lowerUser.contains("haha") || lowerUser.contains("lol") || lowerUser.contains("funny") -> {
                triggerGesture(com.example.data.model.AvatarGesture.LAUGH_TEASE, 3500)
            }
        }

        viewModelScope.launch {
            _isSending.value = true
            try {
                val response = repository.sendMessage(trimmed)
                speakText(response.replyText)

                val replyLower = response.replyText.lowercase(Locale.ROOT)
                if (replyLower.contains("soap") || replyLower.contains("wash your mouth")) {
                    triggerGesture(com.example.data.model.AvatarGesture.SOAP_DISCIPLINE, 5000)
                } else if (replyLower.contains("sector 9")) {
                    triggerGesture(com.example.data.model.AvatarGesture.SECTOR_9_ATTITUDE, 4500)
                }

                if (response.detectedTask != null) {
                    _toastEvent.emit("Kara added reminder: \"${response.detectedTask.title}\"")
                } else if (response.affinityDelta > 0) {
                    _toastEvent.emit("+${response.affinityDelta} Affinity with Kara!")
                }
            } catch (e: Exception) {
                _toastEvent.emit("Error sending message: ${e.localizedMessage}")
            } finally {
                _isSending.value = false
            }
        }
    }

    fun selectArchetype(archetype: PersonalityArchetype) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(selectedArchetype = archetype.name))
            _toastEvent.emit("Kara's Archetype updated to ${archetype.label}!")
        }
    }

    fun selectRelationshipStage(stage: RelationshipStage) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(manualStageUnlocked = stage.name))
            _toastEvent.emit("Relationship status set to: ${stage.title} ${stage.iconEmoji}")
        }
    }

    fun selectTone(tone: ConversationTone) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(selectedTone = tone.name))
            _toastEvent.emit("Conversation tone set to: ${tone.displayName}")
        }
    }

    fun toggleInteractionMode(mode: InteractionMode) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(currentMode = mode.name))
            _toastEvent.emit("Switched to ${mode.title}")
        }
    }

    fun toggleAutoDetect(enabled: Boolean) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(autoDetectRoleplay = enabled))
            _toastEvent.emit(if (enabled) "Roleplay auto-detect enabled" else "Roleplay auto-detect disabled")
        }
    }

    fun updateWardrobeStyle(wardrobe: com.example.data.model.WardrobeStyle) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(wardrobeStyle = wardrobe.name))
            _toastEvent.emit("Wardrobe changed to ${wardrobe.title} ${wardrobe.iconEmoji}")
            speakText(wardrobe.karaReaction)
        }
    }

    fun updateRoomTheme(theme: com.example.data.model.RoomTheme) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(roomTheme = theme.name))
            _toastEvent.emit("Crib theme updated to ${theme.title} ${theme.iconEmoji}")
        }
    }

    fun updateRoomLighting(lighting: com.example.data.model.RoomLighting) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(roomLighting = lighting.name))
            _toastEvent.emit("Lighting set to ${lighting.title} ${lighting.iconEmoji}")
        }
    }

    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        reminderTime: String
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                reminderTimeFormatted = reminderTime
            )
            repository.insertTask(task)
            _toastEvent.emit("Task added to schedule!")

            // If reminder time was provided, schedule notification
            if (reminderTime.isNotBlank()) {
                val currProfile = profile.value
                val isParent = currProfile?.selectedArchetype == PersonalityArchetype.PARENTAL.name
                val notifTitle = if (isParent) "Kara (Mom Mode) 🧼" else "Kara Reminder 📌"
                val notifMsg = if (isParent) {
                    "Stephen! Time for \"${task.title}\"! Don't make me wash your mouth out with soap, get it done right now! 🧼"
                } else {
                    "Hey Stephen, it's time for: \"${task.title}\"!"
                }
                com.example.ui.notifications.KaraNotificationHelper.scheduleReachOutAlarm(
                    getApplication(),
                    delayMinutes = 10,
                    title = notifTitle,
                    message = notifMsg
                )
            }
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
            if (!task.isCompleted) {
                _toastEvent.emit("Great job Stephen! Task completed 🎉")
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _toastEvent.emit("Task removed")
        }
    }

    fun addMemory(key: String, value: String, category: String) {
        if (key.isBlank() || value.isBlank()) return
        viewModelScope.launch {
            repository.insertMemory(
                UserMemoryEntity(
                    key = key.trim(),
                    value = value.trim(),
                    category = category.trim()
                )
            )
            _toastEvent.emit("Added to Kara's memory bank!")
        }
    }

    fun deleteMemory(memory: UserMemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
            _toastEvent.emit("Memory forgotten")
        }
    }

    fun updateUserName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(userName = trimmed))
            _toastEvent.emit("User name updated to $trimmed")
        }
    }

    fun updateCustomApiKey(key: String) {
        viewModelScope.launch {
            val current = profile.value ?: return@launch
            repository.updateProfile(current.copy(customApiKey = key.trim()))
            _toastEvent.emit(if (key.isBlank()) "Using system Gemini key" else "Custom Gemini API key saved")
        }
    }

    fun isSpeechRecognitionAvailable(): Boolean = speechHelper.isAvailable()

    fun startListening(onResult: (String) -> Unit = {}) {
        speechHelper.startListening(
            onFinalResult = { text ->
                if (text.isNotBlank()) {
                    onResult(text)
                }
            },
            onError = { errMsg ->
                viewModelScope.launch {
                    _toastEvent.emit(errMsg)
                }
            }
        )
    }

    fun stopListening() {
        speechHelper.stopListening()
    }

    fun askKaraWebNews(topic: String = "trending world news") {
        sendMessage("Kara, search the web and tell me the latest news on $topic")
    }

    fun triggerKaraReachOutNotification() {
        val currProfile = profile.value
        val archetype = try {
            PersonalityArchetype.valueOf(currProfile?.selectedArchetype ?: "PARENTAL")
        } catch (e: Exception) {
            PersonalityArchetype.PARENTAL
        }
        val userName = currProfile?.userName ?: "Stephen"
        val pendingCount = pendingTasksCount.value

        val (title, message) = com.example.ui.notifications.KaraNotificationHelper.getReachOutContent(
            archetype = archetype,
            userName = userName,
            pendingTasksCount = pendingCount
        )

        com.example.ui.notifications.KaraNotificationHelper.showNotification(
            getApplication(),
            title = title,
            message = message
        )
        viewModelScope.launch {
            _toastEvent.emit("Kara reached out via notification!")
        }
    }

    fun scheduleBackgroundReachOut(delayMinutes: Long = 15) {
        com.example.ui.notifications.KaraNotificationHelper.scheduleReachOutAlarm(
            getApplication(),
            delayMinutes = delayMinutes
        )
        viewModelScope.launch {
            _toastEvent.emit("Scheduled Kara reach-out in $delayMinutes min")
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            _toastEvent.emit("Chat history cleared")
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
