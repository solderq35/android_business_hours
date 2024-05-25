package com.example.businesshours.data

import android.util.Log
import com.example.businesshours.model.BusinessHoursResponse
import com.example.businesshours.network.BusinessHoursApiService

interface BusinessHoursRepository {
    suspend fun getBusinessHours(): BusinessHoursResponse
}

class NetworkBusinessHoursRepository(private val businessHoursApiService: BusinessHoursApiService) :
    BusinessHoursRepository {
    override suspend fun getBusinessHours(): BusinessHoursResponse {
        val response = businessHoursApiService.getBusinessHoursData()
        Log.d("BusinessHoursRepository", "API Response: $response")
        return response
    }
}
