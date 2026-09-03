package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: return
        val isAdvance = intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_ADVANCE, false)
        val action = intent.action

        val db = AppDatabase.getInstance(context)
        val helper = NotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action == NotificationHelper.ACTION_MARK_COMPLETE) {
                    db.reminderDao().markCompleted(reminderId)
                    return@launch
                }

                val reminder = db.reminderDao().getReminderByIdNoUser(reminderId) ?: return@launch

                if (reminder.isActive && !reminder.isCompleted) {
                    helper.showReminderNotification(reminder, isAdvance)
                    if (isAdvance) {
                        db.reminderDao().markAdvanceNotified(reminderId)
                    }
                }
            } catch (e: Exception) {
                Log.e("ReminderAlarmReceiver", "Error processing alarm", e)
            }
        }
    }
}
