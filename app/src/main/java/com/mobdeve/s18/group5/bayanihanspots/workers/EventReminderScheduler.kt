package com.mobdeve.s18.group5.bayanihanspots.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Scheduler for event reminder notifications.
 * Schedules notifications for 3 days and 1 day before an event.
 */
object EventReminderScheduler {
    private const val TAG = "EventReminderScheduler"
    private const val THREE_DAYS_IN_MILLIS = 3 * 24 * 60 * 60 * 1000L
    private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

    /**
     * Schedule reminders for an event (3 days and 1 day before)
     * @param eventScheduleMillis The event's scheduled time in milliseconds (UTC)
     */
    fun scheduleReminders(
        context: Context,
        eventId: String,
        eventTitle: String,
        eventScheduleMillis: Long
    ) {
        val workManager = WorkManager.getInstance(context)
        val currentTime = System.currentTimeMillis()

        // Schedule 3-day reminder
        val threeDaysBeforeMillis = eventScheduleMillis - THREE_DAYS_IN_MILLIS
        if (threeDaysBeforeMillis > currentTime) {
            val delay = threeDaysBeforeMillis - currentTime
            scheduleReminder(
                workManager = workManager,
                eventId = eventId,
                eventTitle = eventTitle,
                daysUntilEvent = 3,
                delayMillis = delay
            )
            Log.d(TAG, "Scheduled 3-day reminder for $eventTitle (delay: ${delay / 1000 / 60} minutes)")
        }

        // Schedule 1-day reminder
        val oneDayBeforeMillis = eventScheduleMillis - ONE_DAY_IN_MILLIS
        if (oneDayBeforeMillis > currentTime) {
            val delay = oneDayBeforeMillis - currentTime
            scheduleReminder(
                workManager = workManager,
                eventId = eventId,
                eventTitle = eventTitle,
                daysUntilEvent = 1,
                delayMillis = delay
            )
            Log.d(TAG, "Scheduled 1-day reminder for $eventTitle (delay: ${delay / 1000 / 60} minutes)")
        }

        // Schedule day-of reminder (morning of the event)
        val dayOfMillis = eventScheduleMillis - (2 * 60 * 60 * 1000L) // 2 hours before
        if (dayOfMillis > currentTime) {
            val delay = dayOfMillis - currentTime
            scheduleReminder(
                workManager = workManager,
                eventId = eventId,
                eventTitle = eventTitle,
                daysUntilEvent = 0,
                delayMillis = delay
            )
            Log.d(TAG, "Scheduled day-of reminder for $eventTitle")
        }
    }

    private fun scheduleReminder(
        workManager: WorkManager,
        eventId: String,
        eventTitle: String,
        daysUntilEvent: Int,
        delayMillis: Long
    ) {
        val uniqueWorkName = "event_reminder_${eventId}_${daysUntilEvent}days"

        val inputData = Data.Builder()
            .putString(EventReminderWorker.KEY_EVENT_ID, eventId)
            .putString(EventReminderWorker.KEY_EVENT_TITLE, eventTitle)
            .putInt(EventReminderWorker.KEY_DAYS_UNTIL_EVENT, daysUntilEvent)
            .putInt(EventReminderWorker.KEY_NOTIFICATION_ID, "${eventId}_$daysUntilEvent".hashCode())
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("event_reminder")
            .addTag(eventId)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel all reminders for an event (when user leaves/cancels)
     */
    fun cancelReminders(context: Context, eventId: String) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(eventId)
        Log.d(TAG, "Cancelled all reminders for event: $eventId")
    }
}

