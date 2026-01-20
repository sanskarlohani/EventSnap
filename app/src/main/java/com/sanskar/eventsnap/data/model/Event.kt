package com.sanskar.eventsnap.data.model

import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val notes: String = "",
    val source: EventSource = EventSource.USER_CREATED,
    // Only for USER_CREATED events stored in Firestore.
    val createdByUid: String? = null,
    val createdByName: String? = null
)

enum class EventSource {
    API,
    USER_CREATED
}
