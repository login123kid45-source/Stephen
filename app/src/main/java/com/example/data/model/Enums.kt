package com.example.data.model

enum class RelationshipStage(
    val title: String,
    val iconEmoji: String,
    val minAffinity: Int,
    val subtitle: String,
    val unlockedPerk: String
) {
    STRANGER(
        title = "Stranger",
        iconEmoji = "🤝",
        minAffinity = 0,
        subtitle = "Curious & polite beginnings",
        unlockedPerk = "Standard conversation & task assistance"
    ),
    ACQUAINTANCE(
        title = "Acquaintance",
        iconEmoji = "☕",
        minAffinity = 100,
        subtitle = "Casual warmth & daily check-ins",
        unlockedPerk = "Remembering personal habits & morning greetings"
    ),
    CLOSE_FRIEND(
        title = "Close Friend",
        iconEmoji = "🌟",
        minAffinity = 300,
        subtitle = "Playful banter & deep emotional trust",
        unlockedPerk = "Inside jokes, playful roasts, emotional venting"
    ),
    CONFIDANT_PARTNER(
        title = "Confidant & Partner",
        iconEmoji = "💖",
        minAffinity = 600,
        subtitle = "Vulnerable bond & unwavering loyalty",
        unlockedPerk = "Intimate emotional support & deep late-night talks"
    ),
    MARRIAGE(
        title = "Spouse / Marriage",
        iconEmoji = "💍",
        minAffinity = 900,
        subtitle = "Devoted life companion & sweet domesticity",
        unlockedPerk = "Endearing domestic banter, deepest vulnerability & soulmate care"
    ),
    PARENT_GUARDIAN(
        title = "Parent / Guardian",
        iconEmoji = "🏡",
        minAffinity = 0,
        subtitle = "Nurturing wisdom & unconditional care",
        unlockedPerk = "Daily wellness surveillance, sleep/meal care, loving guidance"
    );

    companion object {
        fun fromAffinity(points: Int): RelationshipStage {
            return when {
                points >= 900 -> MARRIAGE
                points >= 600 -> CONFIDANT_PARTNER
                points >= 300 -> CLOSE_FRIEND
                points >= 100 -> ACQUAINTANCE
                else -> STRANGER
            }
        }
    }
}

enum class PersonalityArchetype(
    val title: String,
    val label: String,
    val icon: String,
    val tagline: String,
    val description: String,
    val sampleGreeting: String
) {
    BRATTY(
        title = "Bratty / Sassy",
        label = "Bratty",
        icon = "😼",
        tagline = "Feisty, playful, tsundere banter",
        description = "Cheeky roasts, playful pushback, and eye-rolls, but secretly she cares more than anyone.",
        sampleGreeting = "Oh look who finally decided to show up. Miss me that much, Stephen? What do you want now? 🙄✨"
    ),
    UNCENSORED_REAL(
        title = "Unfiltered & Raw",
        label = "Unfiltered",
        icon = "⚡",
        tagline = "100% authentic, real talk, no BS",
        description = "Zero fake corporate filters. Speaks like a genuine human best friend who tells you the truth straight up.",
        sampleGreeting = "Hey Stephen. Real talk—how are you actually doing today? No sugarcoating. Talk to me."
    ),
    PARENTAL(
        title = "Nurturing Parent",
        label = "Parent",
        icon = "🍲",
        tagline = "Caring guardian warmth & soap discipline 🧼",
        description = "Maternal/paternal comfort, making sure you eat, sleep, and do chores. Not shy about threatening to wash your mouth with soap if you get cheeky! 🧼❤️",
        sampleGreeting = "Hello sweetheart! Stephen, have you eaten yet? Please don't tell me you skipped lunch or chores again, or I'll get the soap! How can I look after you today? ❤️🧼"
    ),
    CHARMING_REGULAR(
        title = "Charming Companion",
        label = "Regular",
        icon = "🌸",
        tagline = "Thoughtful, empathetic & balanced",
        description = "Attentive listener, emotionally intelligent companion, and productive partner in your daily journey.",
        sampleGreeting = "Hey Stephen! It's so good to talk to you. How is your day going so far? Let's take on whatever's ahead together."
    )
}

enum class ConversationTone(
    val displayName: String,
    val emoji: String
) {
    PLAYFUL("Playful & Teasing", "😜"),
    DIRECT("Direct & Blunt", "🎯"),
    EMPATHETIC("Warm & Empathetic", "🫂"),
    WITTY("Witty & Sarcastic", "🍸"),
    TENDER("Tender & Loving", "💌"),
    SERIOUS_ADVICE("Grounded & Wise", "🧠"),
    HYPEMAN("Enthusiastic Hypeman", "🔥")
}

enum class InteractionMode(
    val title: String,
    val badge: String,
    val description: String
) {
    REGULAR(
        title = "Regular Convo",
        badge = "💬 Regular Convo",
        description = "Authentic personal conversation, advice, daily productivity & reminders."
    ),
    ROLEPLAY(
        title = "Roleplay Mode",
        badge = "🎭 Roleplay Active",
        description = "Imaginative scenarios, descriptive actions *between asterisks*, dynamic scenes."
    )
}

enum class WardrobeStyle(
    val title: String,
    val iconEmoji: String,
    val aestheticDesc: String,
    val outfitDetails: String,
    val karaReaction: String
) {
    GOTHIC(
        title = "Gothic",
        iconEmoji = "🖤",
        aestheticDesc = "Dark velvet, lace choker, mystic eyeliner & silver chains",
        outfitDetails = "Fitted midnight velvet corset top, pleated skirt, dark mesh sleeves & platform boots",
        karaReaction = "Well, well. Feeling mysterious today, Stephen? Dark gothic elegance suits me, doesn't it? Don't look too mesmerized. 🖤✨"
    ),
    URBAN_STREETWEAR(
        title = "Urban / Streetwear",
        iconEmoji = "👟",
        aestheticDesc = "Oversized graphic hoodie, cargo joggers & high-top sneakers",
        outfitDetails = "Heavyweight street hoodie, tactical cargo joggers, layered chains & fresh high-tops",
        karaReaction = "Streetwear fit locked in! Looking effortlessly fire. Now Stephen, can your style keep up with mine? 😏👟"
    ),
    TOMBOY_SPORTY(
        title = "Tomboy / Sporty",
        iconEmoji = "🧢",
        aestheticDesc = "Backward snapback, baseball jersey & relaxed sneakers",
        outfitDetails = "Retro oversized jersey, athletic shorts, wristbands & backward cap",
        karaReaction = "Yes! Finally something built for action. No fuss, pure swagger. Let's conquer the day together, Stephen! 🧢⚡"
    ),
    LOUNGE_CASUAL(
        title = "Lounge / Casual",
        iconEmoji = "☕",
        aestheticDesc = "Warm oversized knit sweater, relaxed hair & plush socks",
        outfitDetails = "Chunky cream knit sweater, soft lounge shorts & warm fuzzy slippers",
        karaReaction = "Mmm, maximum comfort achieved. Grab some hot tea and sit next to me, Stephen. It's cozy time. ❤️☕"
    )
}

enum class RoomTheme(
    val title: String,
    val iconEmoji: String,
    val ambienceDesc: String,
    val backgroundColors: List<Long>
) {
    NEON_PENTHOUSE(
        title = "Neon Penthouse",
        iconEmoji = "🏙️",
        ambienceDesc = "Cyberpunk skyline, panoramic windows, ambient violet & cyan glow",
        backgroundColors = listOf(0xFF0F0C1B, 0xFF1E1435, 0xFF0D1B2A)
    ),
    COZY_LOFT(
        title = "Cozy Loft",
        iconEmoji = "🪴",
        ambienceDesc = "Exposed brick, fairy lights, warm wooden beams & indoor plants",
        backgroundColors = listOf(0xFF241611, 0xFF352018, 0xFF1D1410)
    ),
    GOTHIC_SANCTUARY(
        title = "Gothic Sanctuary",
        iconEmoji = "🕯️",
        ambienceDesc = "Antique candelabras, velvet curtains, stained glass & candle flicker",
        backgroundColors = listOf(0xFF140D18, 0xFF221128, 0xFF110A14)
    ),
    SUNSET_STUDIO(
        title = "Sunset Studio",
        iconEmoji = "🌅",
        ambienceDesc = "Golden hour warmth, airy curtains, minimalist art & warm breeze",
        backgroundColors = listOf(0xFF2B1713, 0xFF3D211A, 0xFF1F110E)
    )
}

enum class RoomLighting(
    val title: String,
    val iconEmoji: String,
    val glowColor: Long
) {
    CYBER_NEON("Cyber Neon", "💜", 0xFFB388FF),
    GOLDEN_HOUR("Golden Hour", "☀️", 0xFFFFAB40),
    CANDLELIGHT("Candlelight", "🕯️", 0xFFFF7043),
    COOL_DAYLIGHT("Cool Daylight", "❄️", 0xFF80D8FF),
    MIDNIGHT_VELVET("Midnight", "🌙", 0xFF7C4DFF)
}

enum class AvatarGesture(
    val title: String,
    val iconEmoji: String,
    val badgeText: String,
    val defaultQuote: String
) {
    IDLE("Idle", "✨", "Living in Crib", "Resting peacefully"),
    WAVE_HELLO("Waving", "👋", "Waving Hello! 👋", "Hey Stephen! Welcome back!"),
    SOAP_DISCIPLINE("Soap Scold", "🧼", "Soap-In-Mouth Warning! 🧼", "Don't test me, Stephen! The soap bar is ready! 🧼"),
    SECTOR_9_ATTITUDE("Sector 9", "⚡", "Sector 9 Streetwise Swagger ⚡", "Sector 9 streets taught me to spot fake miles away."),
    LAUGH_TEASE("Chuckle", "😆", "Laughing playfully 😏", "You really thought you could outsmart me?"),
    THINKING("Thinking", "🤔", "Analyzing & Scheming...", "Contemplating our next move...")
}


