/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.businesshours.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.businesshours.BusinessHoursApplication
import com.example.businesshours.data.BusinessHoursRepository
import com.example.businesshours.model.Hour
import java.io.IOException
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** UI state for the Home screen */
sealed interface MarsUiState {
    data class Success(val photos: List<Hour>) : MarsUiState

    object Error : MarsUiState

    object Loading : MarsUiState
}

class BusinessHoursViewModel(private val businessHoursRepository: BusinessHoursRepository) :
    ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    /** Call getMarsPhotos() on init so we can display status immediately. */
    init {
        getBusinessHours()
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the [MarsPhoto]
     * [List] [MutableList].
     */
    fun getBusinessHours() {
        viewModelScope.launch {
            marsUiState = MarsUiState.Loading
            marsUiState =
                try {
                    MarsUiState.Success(businessHoursRepository.getBusinessHours())
                } catch (e: IOException) {
                    MarsUiState.Error
                } catch (e: HttpException) {
                    MarsUiState.Error
                }
        }
    }

    /** Factory for [BusinessHoursViewModel] that takes [BusinessHoursRepository] as a dependency */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BusinessHoursApplication)
                val businessHoursRepository = application.container.businessHoursRepository
                BusinessHoursViewModel(businessHoursRepository = businessHoursRepository)
            }
        }
    }
}
