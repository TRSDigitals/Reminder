package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.models.ReminderItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_IS_ADVANCE = "extra_is_advance"
    }

    fun scheduleReminder(reminder: ReminderItem) {
        if (!reminder.isActive || reminder.isCompleted) return

        try {
            val targetDateTime = parseDateTime(reminder.targetDate, reminder.targetTime)
            val triggerMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val nowMillis = System.currentTimeMillis()

            // 1. Schedule On-Time Alarm
            if (reminder.notifyOnTime && triggerMillis > nowMillis) {
                val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
                    putExtra(EXTRA_REMINDER_ID, reminder.id)
                    putExtra(EXTRA_IS_ADVANCE, false)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminder.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                setExactAlarm(triggerMillis, pendingIntent)
            }

            // 2. Schedule 24-Hour Advance Alert
            if (reminder.notifyAdvance24h && !reminder.advanceNotified) {
                val advanceMillis = triggerMillis - (24 * 60 * 60 * 1000L)
                if (advanceMillis > nowMillis) {
                    val advanceIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
                        putExtra(EXTRA_REMINDER_ID, reminder.id)
                        putExtra(EXTRA_IS_ADVANCE, true)
                    }
                    val advancePendingIntent = PendingIntent.getBroadcast(
                        context,
                        reminder.id.hashCode() + 50000,
                        advanceIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    setExactAlarm(advanceMillis, advancePendingIntent)
                }
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to schedule reminder ${reminder.id}", e)
        }
    }

    fun cancelReminder(reminderId: String) {
        try {
            // Cancel on-time
            val intent = Intent(context, ReminderAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }

            // Cancel advance
            val advancePendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode() + 50000,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (advancePendingIntent != null) {
                alarmManager.cancel(advancePendingIntent)
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to cancel reminder $reminderId", e)
        }
    }

    private fun setExactAlarm(triggerMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    private fun parseDateTime(dateStr: String, timeStr: String): LocalDateTime {
        val date = try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now()
        }

        val time = try {
            LocalTime.parse(timeStr.ifBlank { "07:00" }, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.of(7, 0)
        }

        return LocalDateTime.of(date, time)
    }
}
