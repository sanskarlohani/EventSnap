package com.sanskar.eventsnap.ui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data object AddEvent : Screen()

    @Serializable
    data object Auth : Screen()

    @Serializable
    data object SignUp : Screen()

    @Serializable
    data class EventDetail(
        val eventId: String,
        val source: String,
        val title: String = "",
        val date: String = "",
        val description: String = "",
        val notes: String = ""
    ) : Screen()
}
