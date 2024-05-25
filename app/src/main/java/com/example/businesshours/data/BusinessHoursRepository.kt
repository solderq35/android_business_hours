package com.example.businesshours.data

import android.util.Log
import com.example.businesshours.model.Hour
import com.example.businesshours.network.BusinessHoursApiService

interface BusinessHoursRepository {
    suspend fun getBusinessHours(): List<Hour>
}

class NetworkBusinessHoursRepository(private val businessHoursApiService: BusinessHoursApiService) :
    BusinessHoursRepository {
    override suspend fun getBusinessHours(): List<Hour> {
        val response = businessHoursApiService.getBusinessHoursData()
        Log.d("BusinessHoursRepository", "API Response: $response")
        return response.hours
    }
}
