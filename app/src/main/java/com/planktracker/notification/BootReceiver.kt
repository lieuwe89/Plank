package com.planktracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.planktracker.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val prefs = PreferencesManager(context)
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = prefs.reminderEnabled.first()
                if (enabled) {
                    val hour = prefs.reminderHour.first()
                    val minute = prefs.reminderMinute.first()
                    NotificationHelper.scheduleDailyReminder(context, hour, minute)
                }
            }
        }
    }
}
