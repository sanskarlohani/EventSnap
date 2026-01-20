package com.sanskar.eventsnap

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.sanskar.eventsnap.util.AnalyticsHelper

class EventSnapApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Log uncaught exceptions so we can see the real reason for the crash in Logcat.
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Log.e("EventSnapCrash", "Uncaught exception on thread=${t.name}", e)
            previousHandler?.uncaughtException(t, e)
        }

        runCatching {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Initialize Analytics Helper
            AnalyticsHelper.init(this)

            // Enable Crashlytics collection
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

            // Fetch FCM token (useful for sending test notifications/diagnostics)
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Log.d("FCM", "App FCM token: $token")
                }
                .addOnFailureListener { e ->
                    Log.w("FCM", "Failed to get FCM token", e)
                }
        }.onFailure { e ->
            Log.e("EventSnapApplication", "Firebase initialization failed", e)
        }
    }
}
