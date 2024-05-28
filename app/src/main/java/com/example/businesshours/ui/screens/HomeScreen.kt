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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.businesshours.R
import com.example.businesshours.model.BusinessHours
import com.example.businesshours.model.BusinessHoursResponse
import com.example.businesshours.ui.components.AccordionGroup
import com.example.businesshours.ui.components.AccordionModel
import com.example.businesshours.ui.theme.BusinessHoursTheme
import java.time.DayOfWeek

data class TimeWindow(val startTime: String, val endTime: String, val endTimeNextDay: Boolean)

data class ModifiedBusinessHour(val dayOfWeek: String, val timeWindows: List<TimeWindow>)

@Composable
fun HomeScreen(
    businessHoursUiState: BusinessHoursUiState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (businessHoursUiState) {
        is BusinessHoursUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
        is BusinessHoursUiState.Success -> {
            Column {
                Spacer(modifier = modifier.height(16.dp))
                BusinessNameHeader(
                    businessHoursUiState.response,
                    modifier = modifier.padding(16.dp)
                )
                BusinessHoursGridScreen(businessHoursUiState.response)
            }
        }
        is BusinessHoursUiState.Error -> ErrorScreen(retryAction, modifier = modifier.fillMaxSize())
    }
}

/** The home screen displaying the loading message. */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}

/** The home screen displaying error message with re-attempt button. */
@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error),
            contentDescription = ""
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) { Text(stringResource(R.string.retry)) }
    }
}

/** The home screen displaying hours grid. */
@Composable
fun BusinessHoursGridScreen(
    response: BusinessHoursResponse,
) {

    fun convertAbbreviationToFullDay(abbreviation: String): String {
        return when (abbreviation) {
            "MON" -> "MONDAY"
            "TUE" -> "TUESDAY"
            "WED" -> "WEDNESDAY"
            "THU" -> "THURSDAY"
            "FRI" -> "FRIDAY"
            "SAT" -> "SATURDAY"
            "SUN" -> "SUNDAY"
            else -> throw IllegalArgumentException("Invalid day abbreviation: $abbreviation")
        }
    }

    // Example usage:
    val sortedBusinessHours =
        response.businessHours.sortedWith(
            compareBy<BusinessHours> {
                    DayOfWeek.valueOf(convertAbbreviationToFullDay(it.dayOfWeek))
                }
                .thenBy { it.startLocalTime }
        )

    println(sortedBusinessHours)

    for (hour in sortedBusinessHours) {
        println(hour)
    }
    // Convert original Hour data class to ModifiedBusinessHour
    val businessHoursByDay =
        sortedBusinessHours
            .groupBy { it.dayOfWeek }
            .map { (dayOfWeek, hours) ->
                val timeWindows =
                    hours.map { hour ->
                        TimeWindow(
                            hour.startLocalTime,
                            hour.endLocalTime,
                            hour.endLocalTime < hour.startLocalTime
                        )
                    }
                ModifiedBusinessHour(dayOfWeek, timeWindows)
            }
    println(businessHoursByDay)

    val businessHoursLateNight = mutableListOf<ModifiedBusinessHour>()

    var i = 0
    while (i < businessHoursByDay.size) {
        val currentHour = businessHoursByDay[i]
        val prevIndex = if (i == 0) businessHoursByDay.size - 1 else i - 1
        val nextIndex = (i + 1) % businessHoursByDay.size
        val prevHour = businessHoursByDay[prevIndex]
        val nextHour = businessHoursByDay[nextIndex]

        val modifiedTimeWindows = mutableListOf<TimeWindow>()

        // Iterate through the time windows of the current hour
        for (currentTimeWindow in currentHour.timeWindows) {
            // If the end time of the current window is "24:00"
            if (currentTimeWindow.endTime == "24:00:00") {
                // Check if there's a next day and the next day starts at "00:00"
                if (
                    nextHour.timeWindows.isNotEmpty() &&
                        nextHour.timeWindows.first().startTime == "00:00:00"
                ) {
                    // Modify the end time of the current window and set endTimeNextDay to true
                    modifiedTimeWindows.add(
                        TimeWindow(
                            startTime = currentTimeWindow.startTime,
                            endTime = nextHour.timeWindows.first().endTime,
                            endTimeNextDay = true
                        )
                    )
                    // Skip the next day's window
                    continue
                }
            }

            if (currentTimeWindow.startTime == "00:00:00") {
                // Check if there's a next day and the next day starts at "00:00"
                if (
                    prevHour.timeWindows.isNotEmpty() &&
                        prevHour.timeWindows.last().endTime == "24:00:00"
                ) {
                    continue
                }
            }

            // Add the original window if no modification needed
            modifiedTimeWindows.add(currentTimeWindow)
        }

        // Create a new ModifiedBusinessHour object with modified time windows
        businessHoursLateNight.add(ModifiedBusinessHour(currentHour.dayOfWeek, modifiedTimeWindows))

        i++
    }

    println(businessHoursLateNight)
    // Transform the data
    val rows =
        businessHoursLateNight
            .flatMap { businessHour ->
                businessHour.timeWindows.map { timeWindow ->
                    AccordionModel.Row(
                        security = businessHour.dayOfWeek,
                        price = "${timeWindow.startTime} - ${timeWindow.endTime}"
                    )
                }
            }
            .toMutableList()

    // Create the AccordionModel
    val modelTechStocks = AccordionModel(header = "Technology Stocks", rows = rows)
    val group = listOf(modelTechStocks)

    AccordionGroup(modifier = Modifier.padding(top = 8.dp), group = group)
}

@Composable
fun BusinessHoursCard(newHour: ModifiedBusinessHour, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = newHour.dayOfWeek, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            newHour.timeWindows.forEach { timeWindow ->
                Text(text = formatTimeWindow(timeWindow), modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun formatTimeWindow(timeWindow: TimeWindow): String {
    val endTime =
        if (timeWindow.endTimeNextDay) {
            "${timeWindow.endTime} (next day)"
        } else {
            timeWindow.endTime
        }
    return "${timeWindow.startTime} - $endTime"
}

@Composable
fun BusinessNameHeader(response: BusinessHoursResponse, modifier: Modifier = Modifier) {
    Text(text = response.locationName, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    BusinessHoursTheme { LoadingScreen() }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    BusinessHoursTheme { ErrorScreen({}) }
}

@Preview(showBackground = true)
@Composable
fun BusinessHoursGridScreenPreview() {
    BusinessHoursTheme {
        val mockData =
            BusinessHoursResponse(
                locationName = "Example Location",
                businessHours = List(10) { BusinessHours("$it", "", "") }
            )
        BusinessHoursGridScreen(mockData)
    }
}
