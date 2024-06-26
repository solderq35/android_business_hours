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
import com.example.businesshours.model.BusinessHoursResponse
import java.io.IOException
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** UI state for the Home screen */
sealed interface BusinessHoursUiState {
    data class Success(val response: BusinessHoursResponse) : BusinessHoursUiState

    data object Error : BusinessHoursUiState

    data object Loading : BusinessHoursUiState
}

class BusinessHoursViewModel(private val businessHoursRepository: BusinessHoursRepository) :
    ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var businessHoursUiState: BusinessHoursUiState by mutableStateOf(BusinessHoursUiState.Loading)
        private set

    /** Call getBusinessHours() on init so we can display status immediately. */
    init {
        getBusinessHours()
    }

    /** Gets Business Hours information from BusinessHoursApiService */
    fun getBusinessHours() {
        viewModelScope.launch {
            businessHoursUiState = BusinessHoursUiState.Loading
            businessHoursUiState =
                try {
                    BusinessHoursUiState.Success(businessHoursRepository.getBusinessHours())
                } catch (e: IOException) {
                    BusinessHoursUiState.Error
                } catch (e: HttpException) {
                    BusinessHoursUiState.Error
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
