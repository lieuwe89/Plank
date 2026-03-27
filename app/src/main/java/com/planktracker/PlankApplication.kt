package com.planktracker

import android.app.Application
import com.planktracker.notification.NotificationHelper

class PlankApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
