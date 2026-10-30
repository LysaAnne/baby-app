package dk.babyapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BabyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // The daily check is cheap and idempotent; it only notifies when something in the programme is due.
        dk.babyapp.reminders.HealthReminderScheduler.ensureScheduled(this)
        dk.babyapp.reminders.DevelopmentReminderScheduler.ensureScheduled(this)
    }
}
