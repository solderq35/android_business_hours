package com.example.businesshours.network

import com.example.businesshours.model.BusinessHoursResponse
import retrofit2.http.GET

interface BusinessHoursApiService {
    @GET("location.json") suspend fun getBusinessHoursData(): BusinessHoursResponse
}
