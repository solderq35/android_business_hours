package com.example.businesshours.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Hour(
    @SerialName("day_of_week") val dayOfWeek: String,
    @SerialName("start_local_time") val startLocalTime: String,
    @SerialName("end_local_time") val endLocalTime: String
)

@Serializable
data class BusinessHoursResponse(
    @SerialName("location_name") val locationName: String,
    @SerialName("hours") val hours: List<Hour>
)
