package com.example

import com.example.ai.KaraConversationEngine
import com.example.data.model.InteractionMode
import com.example.data.model.PersonalityArchetype
import com.example.data.model.RelationshipStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KaraEngineTest {

    private val engine = KaraConversationEngine()

    @Test
    fun testRoleplayDetection_withAsterisks() {
        val input = "*smiles and steps forward* Hello Kara!"
        val mode = engine.detectMode(input, InteractionMode.REGULAR, autoDetect = true)
        assertEquals(InteractionMode.ROLEPLAY, mode)
    }

    @Test
    fun testRoleplayDetection_regularKeywords() {
        val input = "Remind me to drink water and check my schedule"
        val mode = engine.detectMode(input, InteractionMode.ROLEPLAY, autoDetect = true)
        assertEquals(InteractionMode.REGULAR, mode)
    }

    @Test
    fun testTaskExtraction() {
        val input = "Kara, please remind me to drink water in 30 minutes"
        val task = engine.extractTaskOrReminder(input)
        assertNotNull(task)
        assertEquals("Drink water in 30 minutes", task?.title)
        assertEquals("Health", task?.category)
    }

    @Test
    fun testMemoryExtraction() {
        val input = "Remember that my favorite drink is iced matcha latte"
        val memory = engine.extractLearnedMemory(input)
        assertNotNull(memory)
    }

    @Test
    fun testRelationshipAffinityMapping() {
        assertEquals(RelationshipStage.STRANGER, RelationshipStage.fromAffinity(50))
        assertEquals(RelationshipStage.ACQUAINTANCE, RelationshipStage.fromAffinity(150))
        assertEquals(RelationshipStage.CLOSE_FRIEND, RelationshipStage.fromAffinity(400))
        assertEquals(RelationshipStage.CONFIDANT_PARTNER, RelationshipStage.fromAffinity(750))
        assertEquals(RelationshipStage.MARRIAGE, RelationshipStage.fromAffinity(1000))
    }

    @Test
    fun testArchetypesExist() {
        val archetypes = PersonalityArchetype.values()
        assertEquals(4, archetypes.size)
        assertTrue(archetypes.any { it == PersonalityArchetype.BRATTY })
        assertTrue(archetypes.any { it == PersonalityArchetype.UNCENSORED_REAL })
        assertTrue(archetypes.any { it == PersonalityArchetype.PARENTAL })
        assertTrue(archetypes.any { it == PersonalityArchetype.CHARMING_REGULAR })
    }

    @Test
    fun testWardrobeStylesExist() {
        val wardrobes = com.example.data.model.WardrobeStyle.values()
        assertEquals(4, wardrobes.size)
        assertTrue(wardrobes.any { it == com.example.data.model.WardrobeStyle.GOTHIC })
        assertTrue(wardrobes.any { it == com.example.data.model.WardrobeStyle.URBAN_STREETWEAR })
        assertTrue(wardrobes.any { it == com.example.data.model.WardrobeStyle.TOMBOY_SPORTY })
        assertTrue(wardrobes.any { it == com.example.data.model.WardrobeStyle.LOUNGE_CASUAL })
    }

    @Test
    fun testRoomThemesExist() {
        val themes = com.example.data.model.RoomTheme.values()
        assertEquals(4, themes.size)
        assertTrue(themes.any { it == com.example.data.model.RoomTheme.NEON_PENTHOUSE })
        assertTrue(themes.any { it == com.example.data.model.RoomTheme.COZY_LOFT })
        assertTrue(themes.any { it == com.example.data.model.RoomTheme.GOTHIC_SANCTUARY })
        assertTrue(themes.any { it == com.example.data.model.RoomTheme.SUNSET_STUDIO })
    }

    @Test
    fun testWebNewsQueryDetection() {
        val webService = com.example.data.remote.WebKnowledgeService()
        assertTrue(webService.isWebOrNewsQuery("What is in the news today?"))
        assertTrue(webService.isWebOrNewsQuery("Search the web for AI breakthroughs"))
        assertTrue(webService.isWebOrNewsQuery("What's happening in tech?"))
        assertTrue(webService.isWebOrNewsQuery("Latest on climate and space"))
        org.junit.Assert.assertFalse(webService.isWebOrNewsQuery("Good morning Kara, how are you?"))
    }

    @Test
    fun testParentReachOutNotificationContainsSoap() {
        val (title, message) = com.example.ui.notifications.KaraNotificationHelper.getReachOutContent(
            archetype = PersonalityArchetype.PARENTAL,
            userName = "Stephen",
            pendingTasksCount = 3
        )
        assertTrue(title.contains("Mom Mode") || title.contains("Kara"))
        assertTrue(message.isNotBlank())
        assertTrue(message.contains("Stephen"))
    }

    @Test
    fun testAvatarGesturesExist() {
        val gestures = com.example.data.model.AvatarGesture.values()
        assertEquals(6, gestures.size)
        assertTrue(gestures.any { it == com.example.data.model.AvatarGesture.WAVE_HELLO })
        assertTrue(gestures.any { it == com.example.data.model.AvatarGesture.SOAP_DISCIPLINE })
        assertTrue(gestures.any { it == com.example.data.model.AvatarGesture.SECTOR_9_ATTITUDE })
        assertTrue(gestures.any { it == com.example.data.model.AvatarGesture.LAUGH_TEASE })
        assertTrue(gestures.any { it == com.example.data.model.AvatarGesture.THINKING })
    }
}
