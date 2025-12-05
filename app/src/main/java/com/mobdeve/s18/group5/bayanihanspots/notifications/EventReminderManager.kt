package com.mobdeve.s18.group5.bayanihanspots.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.MainActivity
import com.mobdeve.s18.group5.bayanihanspots.R
import java.util.Date

/**
 * Handles scheduling and displaying event reminder notifications.
 */
object EventReminderManager {
    private const val TAG = "EventReminderManager"
    private const val CHANNEL_ID = "event_reminders"
    private const val CHANNEL_NAME = "Event Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming volunteer events"

    /**
     * Create the notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * Schedule a reminder notification for an event
     * @param context Application context
     * @param eventId The event ID
     * @param eventTitle The event title
     * @param triggerTimeMillis When to trigger the notification (in milliseconds)
     * @param reminderType Description like "starts now", "in 1 hour", "tomorrow", etc.
     */
    fun scheduleReminder(
        context: Context,
        eventId: String,
        eventTitle: String,
        triggerTimeMillis: Long,
        reminderType: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra("eventId", eventId)
            putExtra("eventTitle", eventTitle)
            putExtra("reminderType", reminderType)
        }

        // Use eventId hashcode + reminderType hashcode for unique request code
        val requestCode = (eventId.hashCode() + reminderType.hashCode()) and 0x7FFFFFFF

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // For exact timing, we need to check permissions on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled exact reminder for $eventTitle at $triggerTimeMillis ($reminderType)")
                } else {
                    // Fall back to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled inexact reminder for $eventTitle at $triggerTimeMillis ($reminderType)")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled reminder for $eventTitle at $triggerTimeMillis ($reminderType)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule reminder: ${e.message}")
        }
    }

    /**
     * Schedule all reminders for an event (at event time, 1 hour before, 1 day before)
     */
    fun scheduleEventReminders(
        context: Context,
        eventId: String,
        eventTitle: String,
        eventTimeMillis: Long
    ) {
        val now = System.currentTimeMillis()

        // Reminder at event time
        if (eventTimeMillis > now) {
            scheduleReminder(context, eventId, eventTitle, eventTimeMillis, "starts now")
        }

        // Reminder 1 hour before
        val oneHourBefore = eventTimeMillis - (60 * 60 * 1000)
        if (oneHourBefore > now) {
            scheduleReminder(context, eventId, eventTitle, oneHourBefore, "starts in 1 hour")
        }

        // Reminder 1 day before
        val oneDayBefore = eventTimeMillis - (24 * 60 * 60 * 1000)
        if (oneDayBefore > now) {
            scheduleReminder(context, eventId, eventTitle, oneDayBefore, "is tomorrow")
        }

        // Reminder 3 days before
        val threeDaysBefore = eventTimeMillis - (3 * 24 * 60 * 60 * 1000)
        if (threeDaysBefore > now) {
            scheduleReminder(context, eventId, eventTitle, threeDaysBefore, "is in 3 days")
        }
    }

    /**
     * Cancel all reminders for an event
     */
    fun cancelEventReminders(context: Context, eventId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderTypes = listOf("starts now", "starts in 1 hour", "is tomorrow", "is in 3 days")

        reminderTypes.forEach { reminderType ->
            val intent = Intent(context, EventReminderReceiver::class.java)
            val requestCode = (eventId.hashCode() + reminderType.hashCode()) and 0x7FFFFFFF
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        Log.d(TAG, "Cancelled reminders for event $eventId")
    }

    /**
     * Show a notification immediately
     */
    @android.annotation.SuppressLint("MissingPermission")
    fun showNotification(
        context: Context,
        eventId: String,
        eventTitle: String,
        reminderType: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigateTo", "events")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Event Reminder")
            .setContentText("\"$eventTitle\" $reminderType!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted")
                // Still save to Firestore even if we can't show system notification
                saveNotificationToFirestore(eventTitle, reminderType)
                return
            }
        }

        val notificationId = (eventId.hashCode() + reminderType.hashCode()) and 0x7FFFFFFF
        NotificationManagerCompat.from(context).notify(notificationId, notification)
        Log.d(TAG, "Showed notification for $eventTitle ($reminderType)")

        // Also save to Firestore for in-app notification
        saveNotificationToFirestore(eventTitle, reminderType)
    }

    /**
     * Save notification to user's Firestore notifications collection
     */
    private fun saveNotificationToFirestore(eventTitle: String, reminderType: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        val notificationData = hashMapOf(
            "title" to "Event Reminder",
            "message" to "\"$eventTitle\" $reminderType!",
            "timestamp" to Date(),
            "isRead" to false
        )

        db.collection("users").document(currentUser.uid)
            .collection("notifications")
            .add(notificationData)
            .addOnSuccessListener {
                Log.d(TAG, "Notification saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save notification: ${e.message}")
            }
    }
}

/**
 * BroadcastReceiver that handles alarm triggers for event reminders
 */
class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra("eventId") ?: return
        val eventTitle = intent.getStringExtra("eventTitle") ?: "Event"
        val reminderType = intent.getStringExtra("reminderType") ?: "is coming up"

        Log.d("EventReminderReceiver", "Received reminder for $eventTitle ($reminderType)")

        EventReminderManager.showNotification(context, eventId, eventTitle, reminderType)
    }
}

/**
 * BroadcastReceiver to reschedule reminders after device reboot
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling event reminders")
            // Reminders need to be rescheduled after reboot
            // This would require fetching joined events from Firestore and rescheduling
            // For now, we'll rely on the app being opened to reschedule
        }
    }
}

