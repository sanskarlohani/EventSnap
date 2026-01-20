package com.sanskar.eventsnap.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sanskar.eventsnap.data.model.Event
import com.sanskar.eventsnap.data.model.EventSource
import com.sanskar.eventsnap.data.model.toEvent
import com.sanskar.eventsnap.data.remote.RetrofitClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

object EventRepository {

    private fun userEventsCollection(): CollectionReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) return null
        return Firebase.firestore.collection("users").document(uid).collection("events")
    }

    private val holidayApiService by lazy {
        RetrofitClient.holidayApiService
    }

    /**
     * Fetch holidays from the public API
     */
    fun getHolidaysFromApi(): Flow<Result<List<Event>>> = flow {
        try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val holidays = holidayApiService.getPublicHolidays(currentYear, "US")
            val events = holidays.map { it.toEvent() }
            emit(Result.success(events))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Observe user-created events from Firestore in real-time
     */
    fun getUserEventsFromFirestore(): Flow<List<Event>> = callbackFlow {
        val collection = userEventsCollection()
        if (collection == null) {
            // Not signed in (or auth not ready) => no user events.
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = collection
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Firestore error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Event(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            date = doc.getString("date") ?: "",
                            description = doc.getString("description") ?: "",
                            notes = doc.getString("notes") ?: "",
                            source = EventSource.USER_CREATED,
                            createdByUid = doc.getString("createdByUid"),
                            createdByName = doc.getString("createdByName")
                        )
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { listener.remove() }
    }.catch {
        Log.e("EventRepository", "Flow error: ${it.message}")
        emit(emptyList())
    }

    /**
     * Add a new event to Firestore
     *
     * Path: users/{uid}/events/{eventId}
     */
    suspend fun addEvent(event: Event): Result<String> {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return Result.failure(IllegalStateException("User must be signed in to add events"))

        val collection = userEventsCollection()
            ?: return Result.failure(Exception("User events collection not initialized"))

        return try {
            val uid = user.uid
            val name = user.displayName ?: user.email

            val eventData = hashMapOf(
                "title" to event.title,
                "date" to event.date,
                "description" to event.description,
                "notes" to event.notes,
                "source" to EventSource.USER_CREATED.name,
                // Optional, still useful even though events are already under the user
                "createdByUid" to uid,
                "createdByName" to name
            )
            val documentRef = collection.add(eventData).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a single event by ID from Firestore
     *
     * Path: users/{uid}/events/{eventId}
     */
    suspend fun getEventById(eventId: String): Result<Event?> {
        val collection = userEventsCollection()
            ?: return Result.failure(IllegalStateException("User must be signed in to load events"))

        return try {
            val document = collection.document(eventId).get().await()
            val event = if (document.exists()) {
                Event(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    date = document.getString("date") ?: "",
                    description = document.getString("description") ?: "",
                    notes = document.getString("notes") ?: "",
                    source = EventSource.USER_CREATED,
                    createdByUid = document.getString("createdByUid"),
                    createdByName = document.getString("createdByName")
                )
            } else null
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an event from Firestore
     *
     * Path: users/{uid}/events/{eventId}
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        val collection = userEventsCollection()
            ?: return Result.failure(IllegalStateException("User must be signed in to delete events"))

        return try {
            collection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
