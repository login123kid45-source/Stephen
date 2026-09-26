package com.example.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.model.PersonalityArchetype
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KaraNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TITLE = "extra_notification_title"
        const val EXTRA_MESSAGE = "extra_notification_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val customTitle = intent.getStringExtra(EXTRA_TITLE)
        val customMessage = intent.getStringExtra(EXTRA_MESSAGE)

        if (!customTitle.isNullOrBlank() && !customMessage.isNullOrBlank()) {
            KaraNotificationHelper.showNotification(context, customTitle, customMessage)
            return
        }

        // Fetch user profile and pending tasks asynchronously
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val profile = db.karaDao().getProfileSync()
                val tasks = db.karaDao().getPendingTasksCountSync()

                val archetype = try {
                    PersonalityArchetype.valueOf(profile?.selectedArchetype ?: "PARENTAL")
                } catch (e: Exception) {
                    PersonalityArchetype.PARENTAL
                }

                val userName = profile?.userName ?: "Stephen"
                val (title, message) = KaraNotificationHelper.getReachOutContent(
                    archetype = archetype,
                    userName = userName,
                    pendingTasksCount = tasks
                )

                KaraNotificationHelper.showNotification(context, title, message)
            } catch (e: Exception) {
                // Fallback default notification
                KaraNotificationHelper.showNotification(
                    context,
                    "Kara (Mom Mode) 🧼",
                    "Stephen, don't forget to finish your tasks, or I'll wash your mouth out with soap! 🧼 Drink some water, sweetheart!"
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
