package com.sanskar.eventsnap.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    private fun isInitialized(): Boolean = firebaseAnalytics != null

    /**
     * Log app open event
     */
    fun logAppOpen() {
        if (!isInitialized()) return
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
    }

    /**
     * Log event when user adds a new event
     */
    fun logEventAdded(eventTitle: String) {
        if (!isInitialized()) return
        val bundle = Bundle().apply {
            putString("event_title", eventTitle)
            putLong("timestamp", System.currentTimeMillis())
        }
        firebaseAnalytics?.logEvent("event_added", bundle)
    }

    /**
     * Log event when user views an event detail
     */
    fun logEventViewed(eventId: String, eventTitle: String) {
        if (!isInitialized()) return
        val bundle = Bundle().apply {
            putString("event_id", eventId)
            putString("event_title", eventTitle)
        }
        firebaseAnalytics?.logEvent("event_viewed", bundle)
    }

    /**
     * Log screen view
     */
    fun logScreenView(screenName: String) {
        if (!isInitialized()) return
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}

