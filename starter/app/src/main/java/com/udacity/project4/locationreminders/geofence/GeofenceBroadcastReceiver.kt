package com.udacity.project4.locationreminders.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.utils.sendNotification

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

//TODO: implement the onReceive method to receive the geofencing events at the background
        if(intent.action == "action_geofence"){
            val geofencingEvent =GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                Log.e("GeofenceBroadcastRecive", geofencingEvent.errorCode.toString())
                return
            }

            if(geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                GeofenceTransitionsJobIntentService.enqueueWork(context,intent)
            }
        }
    }
}