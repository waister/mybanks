package com.duduapps.mybanks.features.removeads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duduapps.mybanks.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoveAdsViewModel(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemoveAdsUiState())
    val uiState: StateFlow<RemoveAdsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RemoveAdsEvent>()
    val events: SharedFlow<RemoveAdsEvent> = _events.asSharedFlow()

    companion object {
        private const val ONE_DAY_MILLIS: Long = 24 * 60 * 60 * 1000L
    }

    init {
        checkPlan()
    }

    fun checkPlan() {
        val hasPlan = preferencesRepository.havePlan()
        val duration = preferencesRepository.planVideoDuration
        val planDuration = if (duration > 0) duration else ONE_DAY_MILLIS

        val remainingDays = if (hasPlan) {
            val expiration = preferencesRepository.planVideoMillis + planDuration
            ((expiration - System.currentTimeMillis()) / ONE_DAY_MILLIS) + 1
        } else {
            planDuration / ONE_DAY_MILLIS
        }

        _uiState.update {
            it.copy(
                hasPlan = hasPlan,
                remainingDays = remainingDays,
            )
        }
    }

    fun onAdLoaded(isLoaded: Boolean) {
        _uiState.update { it.copy(isAdLoaded = isLoaded, isLoading = false) }
    }

    fun onWatchAdClicked() {
        viewModelScope.launch {
            _events.emit(RemoveAdsEvent.ShowRewardedAd)
        }
    }

    fun onUserEarnedReward() {
        preferencesRepository.planVideoMillis = System.currentTimeMillis()
        checkPlan()
        viewModelScope.launch {
            _events.emit(RemoveAdsEvent.AdRewardSuccess)
        }
    }
}
