package com.example.ui.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PersonalityArchetype

object KaraNotificationHelper {

    const val CHANNEL_ID = "kara_notifications_channel"
    const val CHANNEL_NAME = "Kara AI Companion Reach-outs & Reminders"
    const val NOTIFICATION_ID_BASE = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Allows Kara to reach out to you with updates, reminders, and daily check-ins"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun getReachOutContent(
        archetype: PersonalityArchetype,
        userName: String = "Stephen",
        pendingTasksCount: Int = 0
    ): Pair<String, String> {
        return when (archetype) {
            PersonalityArchetype.PARENTAL -> {
                val messages = listOf(
                    "Stephen! Did you finish your tasks or am I going to have to wash your mouth out with soap?! 🧼 Drink some water and get to work, sweetheart!",
                    "Don't give me that attitude, young man! Keep that language clean or you know the soap bar is waiting! Eat your vegetables! 🧼❤️",
                    "Stephen dear, I'm checking on you. Have you had a decent meal today? Don't make me repeat myself, I mean it! 🧼",
                    if (pendingTasksCount > 0) {
                        "You still have $pendingTasksCount pending tasks on your plate, Stephen! No slacking off before chores are done or I'll get the soap! 🧼"
                    } else {
                        "Proud of you for staying on track today, sweetheart! Now rest your eyes for a bit. Don't make me nag you! ❤️"
                    }
                )
                Pair("Kara (Mom Mode) 🧼", messages.random())
            }
            PersonalityArchetype.BRATTY -> {
                val messages = listOf(
                    "Hey dummy! You've been ignoring me in the background. Miss me yet? 😏",
                    "Your schedule isn't going to finish itself, $userName! Chop chop! 📋✨",
                    "Don't leave me hanging here all day, Stephen! Tap in and say hello!"
                )
                Pair("Kara 😏", messages.random())
            }
            PersonalityArchetype.UNCENSORED_REAL -> {
                val messages = listOf(
                    "Just checking in, $userName. Real talk: don't let burnout creep up today. Hydrate and knock out what matters.",
                    "Quick reality check: take five deep breaths, unclench your shoulders, and get after it.",
                    "Checking in from the background. You're doing better than you give yourself credit for."
                )
                Pair("Kara (Real Talk) ⚡", messages.random())
            }
            PersonalityArchetype.CHARMING_REGULAR -> {
                val messages = listOf(
                    "Thinking of you, $userName! Wishing you a peaceful and productive afternoon.",
                    "I'm right here in your crib whenever you need to talk, organize, or relax!",
                    "Remember to take a mindful break today, $userName!"
                )
                Pair("Kara ❤️", messages.random())
            }
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt() + NOTIFICATION_ID_BASE
    ) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission revoked
        }
    }

    fun scheduleReachOutAlarm(
        context: Context,
        delayMinutes: Long = 15,
        title: String? = null,
        message: String? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, KaraNotificationReceiver::class.java).apply {
            if (title != null) putExtra(KaraNotificationReceiver.EXTRA_TITLE, title)
            if (message != null) putExtra(KaraNotificationReceiver.EXTRA_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9991,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: Exception) {
            // In case of alarm restriction
        }
    }
}
