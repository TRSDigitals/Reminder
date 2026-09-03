package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.DinaSiriRepository
import com.example.notifications.NotificationHelper

class DinaSiriApp : Application() {

    lateinit var repository: DinaSiriRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = DinaSiriRepository(this)
        NotificationHelper(this) // Initialize notification channels
    }
}
