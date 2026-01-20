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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AddEventUiState(
    val title: String = "",
    val date: String = "",
    val notes: String = "",
    val titleError: String? = null,
    val dateError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AddEventViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState: StateFlow<AddEventUiState> = _uiState.asStateFlow()

    init {
        AnalyticsHelper.logScreenView("AddEventScreen")
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = null
        )
    }

    fun updateDate(date: String) {
        _uiState.value = _uiState.value.copy(
            date = date,
            dateError = null
        )
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun validateAndSubmit() {
        val currentState = _uiState.value
        var hasError = false

        // Validate title
        if (currentState.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = "Title is required")
            hasError = true
        }

        // Validate date
        if (currentState.date.isBlank()) {
            _uiState.value = _uiState.value.copy(dateError = "Date is required")
            hasError = true
        } else {
            // Validate date format (YYYY-MM-DD)
            try {
                LocalDate.parse(currentState.date, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(dateError = "Invalid date format (use YYYY-MM-DD)")
                hasError = true
            }
        }

        if (hasError) return

        submitEvent()
    }

    private fun submitEvent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val event = Event(
                title = _uiState.value.title.trim(),
                date = _uiState.value.date,
                description = "User created event",
                notes = _uiState.value.notes.trim(),
                source = EventSource.USER_CREATED
            )

            val result = EventRepository.addEvent(event)

            result.onSuccess {
                AnalyticsHelper.logEventAdded(event.title)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = "Failed to save event: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = AddEventUiState()
    }
}

