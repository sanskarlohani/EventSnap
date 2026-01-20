package com.sanskar.eventsnap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.eventsnap.data.model.Event
import com.sanskar.eventsnap.data.model.EventSource
import com.sanskar.eventsnap.data.repository.EventRepository
import com.sanskar.eventsnap.util.AnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class EventDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        AnalyticsHelper.logScreenView("EventDetailScreen")
    }

    fun loadEvent(eventId: String, source: EventSource) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            if (source == EventSource.API) {
                // For API events, we pass the event data through navigation
                _uiState.value = _uiState.value.copy(isLoading = false)
            } else {
                // For Firestore events, fetch from database
                val result = EventRepository.getEventById(eventId)
                result.onSuccess { event ->
                    if (event != null) {
                        AnalyticsHelper.logEventViewed(event.id, event.title)
                    }
                    _uiState.value = _uiState.value.copy(
                        event = event,
                        isLoading = false,
                        error = if (event == null) "Event not found" else null
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load event: ${e.message}"
                    )
                }
            }
        }
    }

    fun setEventFromNavigation(event: Event) {
        AnalyticsHelper.logEventViewed(event.id, event.title)
        _uiState.value = _uiState.value.copy(
            event = event,
            isLoading = false
        )
    }

    fun deleteEvent() {
        val currentEvent = _uiState.value.event ?: return

        if (currentEvent.source == EventSource.API) {
            _uiState.value = _uiState.value.copy(error = "Cannot delete API events")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = EventRepository.deleteEvent(currentEvent.id)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isDeleted = true
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete event: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

