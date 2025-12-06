package com.mobdeve.s18.group5.bayanihanspots

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceBroadcastReceiver", errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
            triggeringGeofences.forEach { geofence ->
                Log.d("GeofenceBroadcastReceiver", "Entered geofence: ${geofence.requestId}")
                // Here you can handle the check-in logic, e.g., show a notification
            }
        } else {
            Log.e("GeofenceBroadcastReceiver", "Invalid transition type: $geofenceTransition")
        }
    }
}
