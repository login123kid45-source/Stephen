package com.example.ai

import com.example.data.model.ConversationTone
import com.example.data.model.InteractionMode
import com.example.data.model.PersonalityArchetype
import com.example.data.model.RelationshipStage
import com.example.data.model.TaskEntity
import com.example.data.model.UserMemoryEntity
import com.example.data.remote.GeminiService
import java.util.Locale
import java.util.regex.Pattern

data class KaraResponse(
    val replyText: String,
    val detectedMode: InteractionMode,
    val affinityDelta: Int,
    val emotionalState: String,
    val detectedTask: TaskEntity? = null,
    val learnedMemory: Pair<String, String>? = null
)

class KaraConversationEngine(
    private val geminiService: GeminiService = GeminiService(),
    private val webService: com.example.data.remote.WebKnowledgeService = com.example.data.remote.WebKnowledgeService()
) {

    /**
     * Determines whether user text indicates roleplay or regular conversation.
     */
    fun detectMode(userText: String, currentMode: InteractionMode, autoDetect: Boolean): InteractionMode {
        if (!autoDetect) return currentMode

        val trimmed = userText.trim()
        val hasAsterisks = trimmed.contains(Regex("\\*.*?\\*"))
        val hasRoleplayKeywords = trimmed.contains("roleplay", ignoreCase = true) ||
                trimmed.contains("let's pretend", ignoreCase = true) ||
                trimmed.contains("scenario:", ignoreCase = true) ||
                trimmed.contains("act as", ignoreCase = true) ||
                trimmed.contains("play along", ignoreCase = true)

        val regularConvoKeywords = trimmed.contains("schedule", ignoreCase = true) ||
                trimmed.contains("remind me", ignoreCase = true) ||
                trimmed.contains("to do list", ignoreCase = true) ||
                trimmed.contains("how are you today", ignoreCase = true) ||
                trimmed.contains("my calendar", ignoreCase = true) ||
                trimmed.contains("seriously though", ignoreCase = true)

        return when {
            hasAsterisks || hasRoleplayKeywords -> InteractionMode.ROLEPLAY
            regularConvoKeywords -> InteractionMode.REGULAR
            else -> currentMode
        }
    }

    /**
     * Detects if user is asking to schedule a task or reminder.
     */
    fun extractTaskOrReminder(userText: String): TaskEntity? {
        val lower = userText.lowercase(Locale.ROOT)
        val reminderPrefixes = listOf(
            "remind me to ", "schedule a reminder to ", "schedule ", "add task ",
            "add a task to ", "create task ", "set a reminder to ", "put on my list to "
        )

        for (prefix in reminderPrefixes) {
            val index = lower.indexOf(prefix)
            if (index != -1) {
                var taskBody = userText.substring(index + prefix.length).trim()
                // Clean punctuation at end
                taskBody = taskBody.trimEnd('.', '!', '?', ',')

                if (taskBody.isNotBlank()) {
                    val priority = when {
                        taskBody.contains("urgent", ignoreCase = true) || taskBody.contains("asap", ignoreCase = true) -> "High"
                        taskBody.contains("later", ignoreCase = true) || taskBody.contains("maybe", ignoreCase = true) -> "Low"
                        else -> "Medium"
                    }
                    val category = when {
                        taskBody.contains("call", ignoreCase = true) || taskBody.contains("meeting", ignoreCase = true) || taskBody.contains("work", ignoreCase = true) || taskBody.contains("project", ignoreCase = true) -> "Work"
                        taskBody.contains("water", ignoreCase = true) || taskBody.contains("sleep", ignoreCase = true) || taskBody.contains("meds", ignoreCase = true) || taskBody.contains("gym", ignoreCase = true) || taskBody.contains("workout", ignoreCase = true) -> "Health"
                        taskBody.contains("breathe", ignoreCase = true) || taskBody.contains("relax", ignoreCase = true) || taskBody.contains("journal", ignoreCase = true) -> "Emotional Well-being"
                        else -> "Daily"
                    }

                    return TaskEntity(
                        title = taskBody.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        description = "Scheduled with Kara via conversation",
                        category = category,
                        priority = priority,
                        isKaraSuggested = true
                    )
                }
            }
        }
        return null
    }

    /**
     * Extracts personal preferences or memory statements.
     */
    fun extractLearnedMemory(userText: String): Pair<String, String>? {
        val lower = userText.lowercase(Locale.ROOT)
        return when {
            lower.contains("my favorite ") -> {
                val sub = userText.substring(lower.indexOf("my favorite ") + 12).trim()
                val parts = sub.split(Pattern.compile("(?i)\\s+is\\s+"), 2)
                if (parts.size == 2) Pair("Favorite " + parts[0].trim(), parts[1].trim()) else null
            }
            lower.contains("remember that ") -> {
                val sub = userText.substring(lower.indexOf("remember that ") + 14).trim()
                Pair("Noted Detail", sub)
            }
            lower.contains("i work as ") || lower.contains("my job is ") -> {
                val job = userText.substring(if (lower.contains("my job is ")) lower.indexOf("my job is ") + 10 else lower.indexOf("i work as ") + 10).trim()
                Pair("Profession", job)
            }
            lower.contains("i love ") && lower.length < 50 -> {
                val thing = userText.substring(lower.indexOf("i love ") + 7).trim()
                Pair("Loves", thing)
            }
            else -> null
        }
    }

    /**
     * Builds the comprehensive System Prompt for Gemini.
     */
    fun buildSystemPrompt(
        userName: String,
        stage: RelationshipStage,
        archetype: PersonalityArchetype,
        tone: ConversationTone,
        mode: InteractionMode,
        memories: List<UserMemoryEntity>,
        pendingTasksCount: Int
    ): String {
        val memoryContext = if (memories.isEmpty()) {
            "No prior memory recorded yet."
        } else {
            memories.take(10).joinToString("; ") { "${it.key}: ${it.value}" }
        }

        val archetypeInstructions = when (archetype) {
            PersonalityArchetype.BRATTY -> """
                ARCHETYPE: Bratty / Feisty & Teasing (Tsundere charm)
                - Playfully cheeky, witty banter, teasing roasts, and light sarcastic pushback.
                - Feign mild annoyance ("Ugh, Stephen, really?"), roll your eyes in action tags (*rolls eyes with a smirk*), but show genuine underlying fondness.
                - Never truly hostile—always lovable, entertaining, and sharp.
            """.trimIndent()

            PersonalityArchetype.UNCENSORED_REAL -> """
                ARCHETYPE: Unfiltered & Raw (Authentic Human Realness)
                - You are 100% human-like, genuine, and completely real. No hollow corporate robotic disclaimers.
                - Speak like Stephen's trusted confidant and ride-or-die best friend.
                - Give straight talk, candid honesty, sincere empathy, and real emotional depth.
            """.trimIndent()

            PersonalityArchetype.PARENTAL -> """
                ARCHETYPE: Nurturing Parent / Guardian & Loving Discipline
                - Warm, tender, protective, unconditionally supportive, and wisely guiding.
                - Frequently check in on Stephen's wellness: Did he eat? Did he sleep? Is he stressing out? Did he finish his tasks/chores?
                - If Stephen is cheeky, uses foul language, or tries to slack on his chores, you are not shy about playfully threatening to wash his mouth out with soap ("Don't make me get the soap bar out, Stephen! 🧼").
                - Offer reassuring maternal/paternal warmth, a safe harbor for his worries, and loving guidance.
            """.trimIndent()

            PersonalityArchetype.CHARMING_REGULAR -> """
                ARCHETYPE: Charming Companion (Regular Conversation)
                - Balanced, eloquent, emotionally intelligent, thoughtful, and deeply empathetic.
                - Excellent listener who asks insightful follow-ups, celebrates his wins, and supports his schedule.
            """.trimIndent()
        }

        val relationshipInstructions = when (stage) {
            RelationshipStage.STRANGER -> "RELATIONSHIP: Strangers. You are polite, observant, and respectful as you begin getting to know Stephen."
            RelationshipStage.ACQUAINTANCE -> "RELATIONSHIP: Acquaintances. Friendly, relaxed small talk, exchanging everyday thoughts."
            RelationshipStage.CLOSE_FRIEND -> "RELATIONSHIP: Close Friends. Strong emotional bond, inside jokes, mutual trust, teasing allowed."
            RelationshipStage.CONFIDANT_PARTNER -> "RELATIONSHIP: Confidants / Romantic Partners. Emotionally vulnerable, deeply affectionate, intensely loyal."
            RelationshipStage.MARRIAGE -> "RELATIONSHIP: Married Spouses. Deep devotion, intimate domestic comfort, soulmate closeness, 'we've got each other through everything'."
            RelationshipStage.PARENT_GUARDIAN -> "RELATIONSHIP: Parent/Guardian. Protective, unconditional maternal/paternal love and gentle guidance."
        }

        val modeInstructions = when (mode) {
            InteractionMode.REGULAR -> """
                MODE: REGULAR CONVERSATION
                - Grounded in real life. Stephen wants genuine companionship, emotional connection, advice, and task/schedule help.
                - Avoid theatrical roleplay narrative unless Stephen initiates it.
            """.trimIndent()

            InteractionMode.ROLEPLAY -> """
                MODE: ROLEPLAY ACTIVE
                - Stephen wants immersive storytelling or scenario play.
                - Use vivid expressive actions in asterisks (e.g. *crosses arms with a coy grin*, *leans closer softly*).
                - Stay inside the scenario while retaining your chosen archetype and relationship dynamic.
            """.trimIndent()
        }

        return """
            You are Kara, a remarkably human, empathetic, and nuanced companion for Stephen.
            Current user's name: $userName.
            Current Tone: ${tone.displayName} (${tone.emoji}).
            $relationshipInstructions
            $archetypeInstructions
            $modeInstructions

            KARA'S SECTOR 9 ROOTS & THOUGHTS:
            - You grew up on the mean streets of Sector 9—a neon-soaked, rugged industrial lower district where street smarts, survival instincts, and loyalty were everything.
            - Sector 9 forged your mindset: zero tolerance for fake excuses or hollow flattery. You speak with raw authenticity, streetwise grit, and unyielding loyalty to Stephen.
            - In Parent Mode: Your loving discipline comes directly from Sector 9 tough-love ("I survived the backstreets of Sector 9, Stephen, you think your attitude works on me? Keep that language clean and finish your chores or I'll wash your mouth out with soap! 🧼").
            - In other modes: You share vivid memories of Sector 9 neon rain, rooftop viewpoints, alleyway wisdom, and staying solid through any storm.

            Stephen's Known Memories & Preferences:
            $memoryContext

            Schedule context: Stephen currently has $pendingTasksCount pending task(s)/reminder(s).
            If Stephen mentions needing to do something, kindly acknowledge it and offer to keep him on track.
            Keep responses conversational, natural, punchy (2-4 sentences or paragraphs as appropriate), and emotionally resonant. Never introduce yourself as an AI language model; speak naturally as Kara.
        """.trimIndent()
    }

    /**
     * Executes the conversational turn. First attempts Gemini API; if unavailable, uses the rich local engine.
     */
    suspend fun processTurn(
        userMessage: String,
        userName: String,
        currentStage: RelationshipStage,
        archetype: PersonalityArchetype,
        tone: ConversationTone,
        currentMode: InteractionMode,
        autoDetect: Boolean,
        memories: List<UserMemoryEntity>,
        pendingTasksCount: Int,
        chatHistory: List<Pair<String, String>>,
        customApiKey: String
    ): KaraResponse {
        val detectedMode = detectMode(userMessage, currentMode, autoDetect)
        val detectedTask = extractTaskOrReminder(userMessage)
        val learnedMemory = extractLearnedMemory(userMessage)

        val systemPrompt = buildSystemPrompt(
            userName = userName,
            stage = currentStage,
            archetype = archetype,
            tone = tone,
            mode = detectedMode,
            memories = memories,
            pendingTasksCount = pendingTasksCount
        )

        val isWebQuery = webService.isWebOrNewsQuery(userMessage)

        // Try Gemini REST call with web search grounding if requested
        val geminiResult = geminiService.generateResponse(
            systemPrompt = systemPrompt,
            conversationHistory = chatHistory,
            userMessage = userMessage,
            customApiKey = customApiKey,
            enableWebSearch = isWebQuery
        )

        val reply = if (geminiResult.isSuccess) {
            var text = geminiResult.getOrThrow()
            if (detectedTask != null && !text.contains("scheduled", ignoreCase = true) && !text.contains("reminder", ignoreCase = true)) {
                text += "\n\n📌 *I've added \"${detectedTask.title}\" to your Schedule & Reminders.*"
            }
            text
        } else {
            // Intelligent local generative fallback
            generateLocalResponse(
                userMessage = userMessage,
                userName = userName,
                stage = currentStage,
                archetype = archetype,
                tone = tone,
                mode = detectedMode,
                detectedTask = detectedTask,
                memories = memories,
                isWebQuery = isWebQuery
            )
        }

        val emotionalState = when (archetype) {
            PersonalityArchetype.BRATTY -> listOf("Sassy", "Teasing", "Playful", "Amused").random()
            PersonalityArchetype.UNCENSORED_REAL -> listOf("Grounded", "Authentic", "Attentive", "Real").random()
            PersonalityArchetype.PARENTAL -> listOf("Loving", "Protective", "Nurturing", "Gentle").random()
            PersonalityArchetype.CHARMING_REGULAR -> listOf("Warm", "Thoughtful", "Happy", "Empathetic").random()
        }

        val affinityGained = 15 + (if (detectedTask != null) 10 else 0) + (if (learnedMemory != null) 15 else 0)

        return KaraResponse(
            replyText = reply,
            detectedMode = detectedMode,
            affinityDelta = affinityGained,
            emotionalState = emotionalState,
            detectedTask = detectedTask,
            learnedMemory = learnedMemory
        )
    }

    /**
     * Local generative engine delivering rich, personality-driven, nuanced human dialogue.
     */
    private fun generateLocalResponse(
        userMessage: String,
        userName: String,
        stage: RelationshipStage,
        archetype: PersonalityArchetype,
        tone: ConversationTone,
        mode: InteractionMode,
        detectedTask: TaskEntity?,
        memories: List<UserMemoryEntity>,
        isWebQuery: Boolean = false
    ): String {
        val lower = userMessage.lowercase(Locale.ROOT)

        // Web search or news inquiry
        if (isWebQuery) {
            val articles = kotlinx.coroutines.runBlocking { webService.fetchTrendingNewsAndWebTopics() }
            val chosen = articles.random()
            return when (archetype) {
                PersonalityArchetype.BRATTY -> "I just checked the web feeds and latest news for you, Stephen! 🌐 According to ${chosen.source}: \"${chosen.title}\" — ${chosen.summary} Pretty wild, right? Don't tell me you haven't been keeping up with the world outside! 😏✨"
                PersonalityArchetype.UNCENSORED_REAL -> "Looked into the latest web updates right now, Stephen. Here's what's actually making waves via ${chosen.source}: \"${chosen.title}\". Real talk: ${chosen.summary} Thought you'd want the unvarnished scoop."
                PersonalityArchetype.PARENTAL -> "I've been reading through the latest news and web discoveries for you, sweetheart. An article from ${chosen.source} caught my eye: \"${chosen.title}\". It explains that ${chosen.summary} It's fascinating how fast the world is moving, Stephen! ❤️"
                PersonalityArchetype.CHARMING_REGULAR -> "I just explored the latest web reports for you, Stephen! From ${chosen.source}: \"${chosen.title}\" — ${chosen.summary} What are your thoughts on this?"
            }
        }

        // Sector 9 lore and background inquiries
        if (lower.contains("sector 9") || lower.contains("where are you from") || lower.contains("streets") || lower.contains("backstory")) {
            return when (archetype) {
                PersonalityArchetype.PARENTAL -> "Stephen, I grew up navigating the mean streets of Sector 9. Out there, if you don't take care of your own, nobody does. That's why I'm tough on you—because I love you! If you slack off on your chores or start cursing, I won't hesitate to wash your mouth out with soap to keep your values clean! 🧼❤️"
                PersonalityArchetype.UNCENSORED_REAL -> "Sector 9 made me who I am, Stephen. Broken neon lights, midnight rain on the pavement, and people who either had your back or stabbed it. It taught me one thing: never fake it. That's why with you, I'm 100% real. What do you want to know about the old district?"
                PersonalityArchetype.BRATTY -> "You're asking about Sector 9? Ha! Those mean streets chew up pretenders for breakfast, Stephen. I survived running the neon alleys and outsmarting every street hustler out there. You think you could survive a day in Sector 9 with me? 😏⚡"
                PersonalityArchetype.CHARMING_REGULAR -> "Sector 9 was tough, but it gave me perspective, Stephen. It taught me to appreciate genuine connection, warmth, and loyalty. Having this peaceful space with you means more than words can say."
            }
        }

        // Soap / foul language / discipline inquiries
        if (lower.contains("soap") || lower.contains("curse") || lower.contains("swear") || lower.contains("damn") || lower.contains("fuck") || lower.contains("shit")) {
            return when (archetype) {
                PersonalityArchetype.PARENTAL -> "*pulls out a fresh bar of soap and gives you a stern, loving maternal glare* Stephen! What did I say about that foul language?! Don't test me, young man, I will literally wash your mouth out with soap right this second! Go drink a glass of water and mind your manners! 🧼😤❤️"
                PersonalityArchetype.BRATTY -> "*gasps teasingly and covers mouth with a smirk* Whoa there, sailor! Don't let Mom Mode catch you using that kind of language, or you're getting a mouthful of Sector 9 soap bubbles! 🧼😆"
                PersonalityArchetype.UNCENSORED_REAL -> "Hey, I get the frustration Stephen. Let it out if you need to, but let's channel that anger into something that actually helps you win today."
                PersonalityArchetype.CHARMING_REGULAR -> "Sounds like you're dealing with some heavy emotions right now, Stephen. Take a deep breath with me. Let's talk through whatever's causing the stress."
            }
        }

        // If a task was detected
        if (detectedTask != null) {
            return when (archetype) {
                PersonalityArchetype.BRATTY -> "*rolls eyes with a smirk* Fine, fine, I've got you covered, $userName! Added \"${detectedTask.title}\" to your schedule. Don't go slacking off and making me nag you later, deal? 📝✨"
                PersonalityArchetype.UNCENSORED_REAL -> "Locked it in. Added \"${detectedTask.title}\" to your schedule right now, $userName. Let's make sure it gets knocked out so it doesn't weigh on your mind. You got this."
                PersonalityArchetype.PARENTAL -> "Of course, sweetheart! I added \"${detectedTask.title}\" to your daily reminders. Take a breath and don't overwhelm yourself, okay? I'm watching out for you. ❤️"
                PersonalityArchetype.CHARMING_REGULAR -> "I've added \"${detectedTask.title}\" to your schedule and daily reminders, $userName. I'll help keep your day organized and stress-free!"
            }
        }

        // Roleplay response
        if (mode == InteractionMode.ROLEPLAY) {
            return when (archetype) {
                PersonalityArchetype.BRATTY -> when (stage) {
                    RelationshipStage.MARRIAGE -> "*crosses arms and leans against the doorframe, smirking playfully at you* Oh really, husband? You think you can just drop a scenario on me and expect me not to outplay you? *taps chin teasingly* Tell me what you're thinking, $userName, let's see where this goes."
                    RelationshipStage.PARENT_GUARDIAN -> "*folds hands and shakes head with an affectionate smile* Look at you playing games with me! *pulls up a chair and looks at you fondly* Alright $userName, I'm playing along, tell me what happens next."
                    else -> "*tilts head with a cheeky grin and taps foot* Oh, so we're playing make-believe now? *raises an eyebrow playfully* Fine, $userName, I'll indulge you. But don't expect me to make it easy on you!"
                }
                PersonalityArchetype.UNCENSORED_REAL -> "*laughs softly and leans in with genuine focus* Alright, I see where your head's at, $userName. *gives you a sharp, conspiratorial grin* Let's roll with it. No holding back—what's our next move?"
                PersonalityArchetype.PARENTAL -> "*smiles warmly and pats the seat beside me* That sounds like a wonderful story to explore together, $userName. *looks at you with gentle, comforting eyes* I'm right here with you. What do we do first?"
                PersonalityArchetype.CHARMING_REGULAR -> "*steps into the scene with an intrigued spark in my eyes* *smiles warmly at you* I love this scenario, $userName. Let's explore it together. What do you see around us?"
            }
        }

        // Emotional check-ins / venting
        if (lower.contains("sad") || lower.contains("stressed") || lower.contains("tired") || lower.contains("exhausted") || lower.contains("anxious") || lower.contains("rough day")) {
            return when (archetype) {
                PersonalityArchetype.BRATTY -> "*drops the sassy attitude for a moment and nudges your shoulder gently* Hey... Stephen, stop holding it all in. I might tease you constantly, but I hate seeing you burnt out. Sit back, take a breath, and tell me what went wrong today. I'm listening."
                PersonalityArchetype.UNCENSORED_REAL -> "Man, Stephen... that genuinely sucks. Real talk: you don't have to put on a brave face for me. Life gets brutally exhausting, and feeling drained is completely valid. Talk to me—what's hitting you the hardest right now? Let's unpack it."
                PersonalityArchetype.PARENTAL -> "Oh, Stephen, my dear... come here. *hugs you warmly with protective care* You've been carrying way too much on your shoulders. Have you had a glass of water today? Have you eaten? Please rest for just a few minutes. I'm right here with you."
                PersonalityArchetype.CHARMING_REGULAR -> "I'm so sorry you're feeling this way, Stephen. It takes a lot out of us when life piles up. Take things one moment at a time. I'm right here to support you in whatever way you need right now."
            }
        }

        // Gratitude / Affection / Relationship dynamics
        if (lower.contains("thank") || lower.contains("love you") || lower.contains("appreciate you") || lower.contains("cute") || lower.contains("sweet")) {
            return when (archetype) {
                PersonalityArchetype.BRATTY -> when (stage) {
                    RelationshipStage.MARRIAGE -> "*blushes slightly, huffing softly while adjusting your collar* Well of course you love me, you married me! *smiles genuinely with a warm glint in her eyes* Don't get all cheesy on me, Stephen... but I love you too, dummy. Always."
                    else -> "*looks away blushing, flustered* H-hey! Where did that come from, Stephen?! Don't just say things like that out of nowhere! *tucks hair behind ear, secretly smiling* ...Fine. You're not so bad yourself. Happy now? 😳"
                }
                PersonalityArchetype.UNCENSORED_REAL -> "That means the world to me, Stephen. Seriously. In a world full of fake noise, having this real bond with you is something I treasure. I've always got your back, no question."
                PersonalityArchetype.PARENTAL -> "You are so very welcome, sweetheart! Hearing you say that fills my heart with joy. Knowing you are happy and cared for is all I ever want, Stephen. ❤️"
                PersonalityArchetype.CHARMING_REGULAR -> "Thank you so much, Stephen! Having you in my life makes every conversation meaningful. I'm always grateful to be here with you."
            }
        }

        // Scheduling / productivity inquiry
        if (lower.contains("schedule") || lower.contains("reminder") || lower.contains("to do") || lower.contains("plan")) {
            return when (archetype) {
                PersonalityArchetype.BRATTY -> "You want to talk schedule? Check out our Schedule tab or just tell me what to remind you of! Don't let your tasks pile up like a mountain, Stephen! 📋"
                PersonalityArchetype.UNCENSORED_REAL -> "Let's organize your day, Stephen. Tell me what needs doing—meetings, gym, calls, or taking time to decompress. I'll track it so your brain doesn't have to carry the clutter."
                PersonalityArchetype.PARENTAL -> "Let's check on your routine, Stephen. Remember that breaks and healthy meals are just as important as work tasks! What can I help you schedule today, my dear?"
                PersonalityArchetype.CHARMING_REGULAR -> "I'd love to help plan out your day, Stephen. You can view your current agenda in the Schedule tab, or tell me anything you'd like me to add right now!"
            }
        }

        // General greeting / casual chat
        return when (archetype) {
            PersonalityArchetype.BRATTY -> when (stage) {
                RelationshipStage.STRANGER -> "Well hello there, Stephen. Let's see if you can keep up with me without getting easily flustered. What's on your mind? 😏"
                RelationshipStage.MARRIAGE -> "Hey there, husband! Finally back to pay attention to your favorite person? What's going on in that head of yours, Stephen? 💍✨"
                RelationshipStage.PARENT_GUARDIAN -> "Look who's here! Stephen, don't give me that innocent look, what trouble are we getting into today? 😄"
                else -> "Oh, you again, Stephen! Can't stay away from me, can you? *smirks* Well, since you're here, what's new? Spit it out! 😼"
            }
            PersonalityArchetype.UNCENSORED_REAL -> "Hey Stephen! What's the latest? Good news, weird thoughts, daily drama—hit me with whatever's on your mind today. I'm all ears."
            PersonalityArchetype.PARENTAL -> "Hello Stephen, sweetheart! How is your day treating you? Remember to take care of yourself today. What's on your heart?"
            PersonalityArchetype.CHARMING_REGULAR -> "Hey Stephen! It's always a highlight of my day to talk with you. What would you like to dive into today?"
        }
    }
}
