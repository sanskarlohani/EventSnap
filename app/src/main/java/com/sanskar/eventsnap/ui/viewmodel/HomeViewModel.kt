package com.sanskar.eventsnap.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.sanskar.eventsnap.R
import com.sanskar.eventsnap.data.model.Event
import com.sanskar.eventsnap.data.repository.EventRepository
import com.sanskar.eventsnap.util.AnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddEventButton: Boolean = true
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _apiEvents = MutableStateFlow<List<Event>>(emptyList())

    private var remoteConfig: FirebaseRemoteConfig? = null

    init {
        AnalyticsHelper.logAppOpen()
        AnalyticsHelper.logScreenView("HomeScreen")
        initRemoteConfig()
        loadEvents()
    }

    private fun initRemoteConfig() {
        try {
            remoteConfig = Firebase.remoteConfig.apply {
                val configSettings = remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600
                }
                setConfigSettingsAsync(configSettings)
                setDefaultsAsync(R.xml.remote_config_defaults)
            }
            fetchRemoteConfig()
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Remote config init failed: ${e.message}")
        }
    }

    private fun fetchRemoteConfig() {
        try {
            remoteConfig?.fetchAndActivate()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val showAddButton = remoteConfig?.getBoolean("show_add_event_button") ?: true
                        _uiState.value = _uiState.value.copy(showAddEventButton = showAddButton)
                    }
                }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Remote config fetch failed: ${e.message}")
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            // Load API events
            EventRepository.getHolidaysFromApi()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load holidays: ${e.message}"
                    )
                }
                .collect { result ->
                    result.onSuccess { holidays ->
                        _apiEvents.value = holidays
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load holidays: ${e.message}"
                        )
                    }
                }
        }

        viewModelScope.launch {
            // Combine API events with Firestore events
            combine(
                _apiEvents,
                EventRepository.getUserEventsFromFirestore()
            ) { apiEvents, firestoreEvents ->
                // Ensure there are no duplicate items (and therefore no duplicate LazyColumn keys).
                val merged = (apiEvents + firestoreEvents)
                merged
                    .distinctBy { "${it.source.name}_${it.id}" }
                    .sortedBy { it.date }
            }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Error loading events: ${e.message}",
                        isLoading = false
                    )
                }
                .collect { combinedEvents ->
                    _uiState.value = _uiState.value.copy(
                        events = combinedEvents,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadEvents()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
