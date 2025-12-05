package com.mobdeve.s18.group5.bayanihanspots.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobdeve.s18.group5.bayanihanspots.notifications.NotificationHelper

/**
 * Worker that shows an event reminder notification.
 * Scheduled by EventReminderScheduler to fire 3 days and 1 day before an event.
 */
class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "EventReminderWorker"
        const val KEY_EVENT_ID = "event_id"
        const val KEY_EVENT_TITLE = "event_title"
        const val KEY_DAYS_UNTIL_EVENT = "days_until_event"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
        val eventTitle = inputData.getString(KEY_EVENT_TITLE) ?: return Result.failure()
        val daysUntilEvent = inputData.getInt(KEY_DAYS_UNTIL_EVENT, -1)
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, eventId.hashCode())

        Log.d(TAG, "Showing reminder for event: $eventTitle ($daysUntilEvent days away)")

        try {
            NotificationHelper.createNotificationChannels(applicationContext)
            NotificationHelper.showEventReminder(
                context = applicationContext,
                eventId = eventId,
                eventTitle = eventTitle,
                daysUntilEvent = daysUntilEvent,
                notificationId = notificationId
            )
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification: ${e.message}")
            return Result.failure()
        }
    }
}

