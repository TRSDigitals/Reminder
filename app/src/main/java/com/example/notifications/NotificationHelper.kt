package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.models.ReminderItem

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "dinasiri_reminders_channel"
        const val CHANNEL_NAME = "ದಿನಸಿರಿ ಜ್ಞಾಪನೆಗಳು (DinaSiri Reminders)"
        const val ACTION_MARK_COMPLETE = "com.example.dinasiri.ACTION_MARK_COMPLETE"
        const val ACTION_SNOOZE = "com.example.dinasiri.ACTION_SNOOZE"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ದಿನಸಿರಿ ಸಮಯೋಚಿತ ಜ್ಞಾಪನೆಗಳು ಹಾಗೂ ಮುನ್ಸೂಚನೆಗಳು"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(reminder: ReminderItem, isAdvance24h: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Open App Intent
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Complete Action
        val completeIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_MARK_COMPLETE
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode() + 1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isAdvance24h) {
            "🔔 ನಾಳಿನ ಜ್ಞಾಪನೆ: ${reminder.title}"
        } else {
            "⏰ ಈಗ ಸಮಯ: ${reminder.title}"
        }

        val content = if (isAdvance24h) {
            "ನಾಳೆ ${reminder.targetTime} ಕ್ಕೆ: ${reminder.description.ifBlank { reminder.title }}"
        } else {
            reminder.description.ifBlank { "ಈ ಕೆಲಸವನ್ನು ಮಾಡಲು ಸಮಯವಾಗಿದೆ: ${reminder.title}" }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "✓ ಮುಗಿದಿದೆ", completePendingIntent)
            .build()

        val notificationId = if (isAdvance24h) reminder.id.hashCode() + 100 else reminder.id.hashCode()
        manager.notify(notificationId, notification)
    }
}
