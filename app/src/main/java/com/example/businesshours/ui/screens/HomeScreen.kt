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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TimeWindow(val startTime: String, val endTime: String, val endTimeNextDay: Boolean)

data class ModifiedBusinessHour(val dayOfWeek: String, val timeWindows: List<TimeWindow>)

data class TimeAndDay(val timeString: String, val dayOfWeek: String)

data class NextTimeWindow(val timeWindow: TimeWindow, val dayOfWeek: String)

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
        Text(
            text = stringResource(R.string.loading_failed),
            modifier = Modifier.padding(16.dp),
            onTextLayout = {}
        )
        Button(onClick = retryAction) { Text(stringResource(R.string.retry), onTextLayout = {}) }
    }
}

/** The home screen displaying hours grid. */
@Composable
fun BusinessHoursGridScreen(
    response: BusinessHoursResponse,
) {

    // testcases (pst)
    // 1716973724000 (wednesday 2:08am) -> wednesday 7am - 1pm
    // 1716970124000 (wednesday 1:08am) -> tuesday 3pm - (wednesday) 2am
    // 1716998924000 (wednesday 9:08am) -> wednesday 7am - 1pm
    // 1717013324000 (wednesday 1:08pm) -> wednesday 3pm - 10pm
    // 1717049324000 (wednesday 11:08pm) -> thursday 24h
    // 1717023600000 (wednesday 3:00pm) -> wednesday 3pm - 10pm
    // TODO: 24h back to back (feed in fake data local var / json)
    // hmm: 1717196400000

    val timestamp = 1717013324000 // Example timestamp in milliseconds
    val result = convertUnixTimestampToLocalTimeStringAndDay(timestamp)
    println("Local time in HH:mm:ss format: ${result.timeString}")
    println("Day of the week: ${result.dayOfWeek}")

    // Example usage:
    val sortedBusinessHours =
        response.businessHours.sortedWith(
            compareBy<BusinessHours> {
                    DayOfWeek.valueOf(convertAbbreviationToAllCaps(it.dayOfWeek))
                }
                .thenBy { it.startLocalTime }
        )

    println("Businesshours Initial Sorting (oldest to newest): ${sortedBusinessHours}")

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
    println("Grouping sorted businesshours by day: ${businessHoursByDay}")

    val businessHoursLateNight = mutableListOf<ModifiedBusinessHour>()

    var i = 0
    while (i < businessHoursByDay.size) {
        val currentHour = businessHoursByDay[i]
        val prevIndex = if (i == 0) businessHoursByDay.size - 1 else i - 1
        val nextIndex = (i + 1) % businessHoursByDay.size
        val prevHour = businessHoursByDay[prevIndex] // TODO: rename these cause confusing (prevBusinessHours?)
        val nextHour = businessHoursByDay[nextIndex] // TODO: rename

        val modifiedTimeWindows = mutableListOf<TimeWindow>()

        // Iterate through the time windows of the current hour
        for (currentTimeWindow in currentHour.timeWindows) {
            // If the end time of the current window is "24:00"
            // TODO: test (24h vs late night, 24h back to back, etc)
            if (currentTimeWindow.endTime == "24:00:00" && currentTimeWindow.startTime != "00:00:00") {
                // Check if there's a next day and the next day starts at "00:00"
                if (
                    nextHour.timeWindows.isNotEmpty() &&
                        nextHour.timeWindows.first().startTime == "00:00:00" &&
                        nextHour.timeWindows.first().endTime != "24:00:00"
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

            if (currentTimeWindow.startTime == "00:00:00" && currentTimeWindow.endTime != "24:00:00") {
                // Check if there's a next day and the next day starts at "00:00"
                if (
                    prevHour.timeWindows.isNotEmpty() &&
                        prevHour.timeWindows.last().endTime == "24:00:00" &&
                        prevHour.timeWindows.last().startTime != "00:00:00"
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

    println("Handling late night edge cases on businesshours grouped by day: ${businessHoursLateNight}")

    val nextTimeWindow = findNextTimeWindow(businessHoursLateNight, result.timeString, result.dayOfWeek)
    println("Next TimeWindow: $nextTimeWindow")

    // Transform the data
    val rows =
        businessHoursLateNight
            .flatMap { businessHour ->
                var firstTimeWindow = true
                businessHour.timeWindows.mapIndexed { index, timeWindow ->
                    val accordionDayOfWeekValue =
                        if (firstTimeWindow) {
                            firstTimeWindow = false
                            convertAbbreviationToFullDay(businessHour.dayOfWeek)
                        } else {
                            ""
                        }
                    val accordionTimeWindowValue =
                        if (index == businessHour.timeWindows.size - 1) {
                            "${convertToConventionalTime(timeWindow.startTime)}-${convertToConventionalTime(timeWindow.endTime)}"
                        } else {

                            // If there are multiple time windows of operating hours for a given day
                            // day, insert commas where needed.
                            // E.g. if Tuesday has "7 AM to 1 PM" and "3 PM to 2 AM", then insert a
                            // comma after "7 AM to 1 PM".
                            "${convertToConventionalTime(timeWindow.startTime)}-${convertToConventionalTime(timeWindow.endTime)},"
                        }
                    AccordionModel.Row(
                        accordionDayOfWeek = accordionDayOfWeekValue,
                        accordionTimeWindow = accordionTimeWindowValue
                    )
                }
            }
            .toMutableList()

    // Create the AccordionModel
    val modelTechStocks = AccordionModel(header = "Open Until PLACEHOLDER", rows = rows)
    val group = listOf(modelTechStocks)

    AccordionGroup(modifier = Modifier.padding(top = 8.dp), group = group)
}

@Composable
fun BusinessNameHeader(response: BusinessHoursResponse, modifier: Modifier = Modifier) {
    Text(text = response.locationName, modifier = modifier, onTextLayout = {})
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

fun convertAbbreviationToFullDay(abbreviation: String): String {
    return when (abbreviation.uppercase(Locale.ROOT)) {
        "MON" -> "Monday"
        "TUE" -> "Tuesday"
        "WED" -> "Wednesday"
        "THU" -> "Thursday"
        "FRI" -> "Friday"
        "SAT" -> "Saturday"
        "SUN" -> "Sunday"
        else -> throw IllegalArgumentException("Invalid day abbreviation: $abbreviation")
    }
}

fun convertAbbreviationToAllCaps(abbreviation: String): String {
    return convertAbbreviationToFullDay(abbreviation).uppercase(Locale.ROOT)
}

fun convertToConventionalTime(time24: String): String {
    val time = LocalTime.parse(time24, DateTimeFormatter.ofPattern("HH:mm:ss"))

    // If minutes value is 00, then just show the hour value, e.g. "7PM"
    return if (time.minute == 0) {
        time.format(DateTimeFormatter.ofPattern("ha"))
    }
    // If minutes value is not 00, then show both hours and minutes, e.g. "7:30PM"
    else {
        time.format(DateTimeFormatter.ofPattern("h:mma"))
    }
}

fun convertUnixTimestampToLocalTimeStringAndDay(timestamp: Long): TimeAndDay {
    // Convert the Unix timestamp to LocalDateTime in the user's local timezone
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())

    // Format the LocalDateTime to HH:mm:ss
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val timeString = dateTime.format(timeFormatter)

    // Get the day of the week as a string
    val dayOfWeekString = dateTime.dayOfWeek.name

    return TimeAndDay(timeString, dayOfWeekString)
}

fun findNextTimeWindow(businessHours: List<ModifiedBusinessHour>, inputTimeString: String, inputDayOfWeek: String): NextTimeWindow? {
    val inputTime = LocalTime.parse(inputTimeString, DateTimeFormatter.ofPattern("HH:mm:ss"))
    val inputDay = inputDayOfWeek.uppercase(Locale.ROOT)

    // Find the index of the input day of the week
    val inputDayIndex = DayOfWeek.valueOf(inputDay).ordinal

    println("WHYYY")
    println(inputTime)
    println(inputDay)
    println(inputDayIndex)

    // Iterate through the days starting from the input day
    for (i in 0 until 7) {
        val currentDayIndex = (inputDayIndex + i) % 7
        val currentDay = DayOfWeek.of(currentDayIndex).name

        println("HDFDFDFD")
        println(currentDayIndex)
        println(currentDay)

        val businessHour = businessHours.find { convertAbbreviationToAllCaps(it.dayOfWeek) == currentDay }
        if (businessHour != null) {
            var previousEndTime: LocalTime? = null

            for ((index, timeWindow) in businessHour.timeWindows.withIndex()) {
                val startTime = LocalTime.parse(timeWindow.startTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                val endTime = LocalTime.parse(timeWindow.endTime, DateTimeFormatter.ofPattern("HH:mm:ss"))

                // test case: wednesday 1 am (1716973724000)
                if (i == 0) {
                    println("PLSSSSS")
                    println(inputTime)
                    println(timeWindow.endTimeNextDay)
                    println(index)
                    println(timeWindow)
                    // println(businessHour.timeWindows[(index + 1) % businessHour.timeWindows.size])
                    if (inputTime.isBefore(endTime) && timeWindow.endTimeNextDay) {
                        println("BROOOdfdfd")
                        println(timeWindow.endTimeNextDay)
                        return NextTimeWindow(timeWindow, currentDay)
                    }
                    // TODO: check "reopen" logic here (e.g. tues 7am-2am(wed), wed 10am-3pm). next time window for current day (actual, not accordion row)
                }
                else if (i == 1) {
                    // Check if inputTime is in one of the time windows (after startTime and before endTime for that time window), for the given day
                    println("HKK")
                    println(index)
                    println(timeWindow)
                    // println(businessHour.timeWindows[(index + 1) % businessHour.timeWindows.size])

                    if (previousEndTime == null && inputTime.isBefore(startTime)) {
                        return NextTimeWindow(timeWindow, currentDay)
                    }

                    if ((inputTime.equals(startTime) || inputTime.isAfter(startTime)) && inputTime.isBefore(endTime)) {
                        return NextTimeWindow(timeWindow, currentDay)
                    }

                    // late night edge case handling
                    if ((inputTime.equals(startTime) || inputTime.isAfter(startTime)) && timeWindow.endTimeNextDay) {
                        return NextTimeWindow(timeWindow, currentDay)
                    }

                    // Check if the inputTime is between two time windows (after endTime of one time window and before startTime of another time window), for the given day
                    if (previousEndTime != null && (inputTime.equals(previousEndTime) || inputTime.isAfter(previousEndTime)) && inputTime.isBefore(startTime)) {
                        return NextTimeWindow(timeWindow, currentDay)
                    }

                    // Track the end time of the current time window
                    previousEndTime = endTime

                    // TODO: check "reopen" logic here (e.g. wed 7am-8am, wed 10am-3pm). next time window for current day (actual, not accordion row). should be easier than i==0 block
                } else {
                    // On subsequent days, return the first window
                    return NextTimeWindow(timeWindow, currentDay)
                }
            }
        }
    }

    return null
}