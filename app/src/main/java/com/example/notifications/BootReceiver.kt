package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            Log.d("BootReceiver", "Device rebooted or time changed, restoring active alarms...")
            val db = AppDatabase.getInstance(context)
            val scheduler = ReminderScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val pendingReminders = db.reminderDao().getAllPendingAlarms()
                    pendingReminders.forEach { reminder ->
                        scheduler.scheduleReminder(reminder)
                    }
                    Log.d("BootReceiver", "Rescheduled ${pendingReminders.size} active reminders.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error restoring reminders", e)
                }
            }
        }
    }
}
