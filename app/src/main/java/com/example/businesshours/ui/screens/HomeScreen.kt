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
import androidx.compose.ui.unit.sp
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

data class FlatBusinessHour(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val endTimeNextDay: Boolean,
    val daysUntilNextTimeWindow: Int,
    val secondsUntilNextTimeWindow: Long,
)

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
    // see https://www.unixtimestamp.com/index.php to convert Unix for debug
    // 1716973724000 (wednesday 2:08am) -> wednesday 7am - 1pm
    // 1716970124000 (wednesday 1:08am) -> tuesday 3pm - (wednesday) 2am
    // 1716998924000 (wednesday 9:08am) -> wednesday 7am - 1pm
    // 1717013324000 (wednesday 1:08pm) -> wednesday 3pm - 10pm
    // 1717049324000 (wednesday 11:08pm) -> thursday 24h
    // 1717023600000 (wednesday 4:00pm) -> wednesday 3pm - 10pm
    // TODO: 24h back to back (feed in fake data local var / json)
    // 1717196400000 (Friday 4:00pm) -> tuesday 7am - 1pm

    // val timestamp = 1716973724000
    val timestamp = System.currentTimeMillis()
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

    println("Businesshours Initial Sorting (oldest to newest): $sortedBusinessHours")

    for (hour in sortedBusinessHours) {
        println(hour)
    }
    // Convert original Hour data class to ModifiedBusinessHour
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
    val flatBusinessHours = flattenBusinessHours(sortedBusinessHours)
    println(flatBusinessHours)

    val nextTimeWindow = findNextTimeWindow(flatBusinessHours, result.timeString, result.dayOfWeek)
    println(nextTimeWindow)

    // Transform the data
    val rows = transformToAccordionRows(flatBusinessHours)

    // Create the AccordionModel
    val modelTechStocks = AccordionModel(header = "$nextTimeWindow", rows = rows)
    val group = listOf(modelTechStocks)

    AccordionGroup(modifier = Modifier.padding(top = 8.dp), group = group)
}

@Composable
fun BusinessNameHeader(response: BusinessHoursResponse, modifier: Modifier = Modifier) {
    Text(
        text = response.locationName,
        fontSize = 24.sp, // Increase text size
        modifier = modifier
    )
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

fun flattenBusinessHours(sortedBusinessHours: List<BusinessHours>): List<FlatBusinessHour> {
    val flatList = mutableListOf<FlatBusinessHour>()

    var i = 0
    while (i < sortedBusinessHours.size) {
        val currentTimeBlock = sortedBusinessHours[i]
        val prevIndex = if (i == 0) sortedBusinessHours.size - 1 else i - 1
        val nextIndex = (i + 1) % sortedBusinessHours.size
        val prevTimeBlock = sortedBusinessHours[prevIndex]
        val nextTimeBlock = sortedBusinessHours[nextIndex]

        val modifiedTimeWindows = mutableListOf<Pair<String, String>>()

        val currentTimeWindow = Pair(currentTimeBlock.startLocalTime, currentTimeBlock.endLocalTime)

        // TODO: remove cause it's relative to the user's time, not between time windows like
        // this...
        val secondsGapVal =
            computeTimeDifference(
                currentTimeBlock.endLocalTime,
                convertAbbreviationToAllCaps(currentTimeBlock.dayOfWeek),
                nextTimeBlock.startLocalTime,
                convertAbbreviationToAllCaps(nextTimeBlock.dayOfWeek)
            )
        println("SECONDS TIME DIFF WINDOW: $secondsGapVal")
        // TODO: handle 2am edge case (endTimeNextDay)
        val daysGapVal =
            daysBetween(
                convertAbbreviationToAllCaps(currentTimeBlock.dayOfWeek),
                convertAbbreviationToAllCaps(nextTimeBlock.dayOfWeek)
            )
        println("DAYS UNTIL NEXT TIME WINDOW: $daysGapVal")

        if (currentTimeWindow.second == "24:00:00" && currentTimeWindow.first != "00:00:00") {
            if (
                nextTimeBlock.startLocalTime == "00:00:00" &&
                    nextTimeBlock.endLocalTime != "24:00:00" &&
                    daysGapVal == 1
            ) {
                modifiedTimeWindows.add(Pair(currentTimeWindow.first, nextTimeBlock.endLocalTime))
            } else {
                modifiedTimeWindows.add(currentTimeWindow)
            }
        } else if (
            currentTimeWindow.first == "00:00:00" && currentTimeWindow.second != "24:00:00"
        ) {
            if (
                !(prevTimeBlock.endLocalTime == "24:00:00" &&
                    prevTimeBlock.startLocalTime != "00:00:00") && daysGapVal == 1
            ) {
                // Skip adding this window
                modifiedTimeWindows.add(currentTimeWindow)
            }
        } else {
            modifiedTimeWindows.add(currentTimeWindow)
        }

        println("modified time windows:")
        println(modifiedTimeWindows)

        for (timeWindow in modifiedTimeWindows) {
            val endTimeNextDay =
                timeWindow.second != "24:00:00" &&
                    nextTimeBlock.startLocalTime == "00:00:00" &&
                    nextTimeBlock.endLocalTime != "24:00:00"
            flatList.add(
                FlatBusinessHour(
                    dayOfWeek = currentTimeBlock.dayOfWeek,
                    startTime = timeWindow.first,
                    endTime = timeWindow.second,
                    endTimeNextDay = endTimeNextDay,
                    daysUntilNextTimeWindow = daysGapVal,
                    secondsUntilNextTimeWindow = secondsGapVal
                )
            )
            val timeDiff =
                computeTimeDifference(
                    timeWindow.second,
                    convertAbbreviationToFullDay(currentTimeBlock.dayOfWeek),
                    nextTimeBlock.startLocalTime,
                    convertAbbreviationToFullDay(nextTimeBlock.dayOfWeek)
                )
            println("TIME DIFF: $timeDiff")
            println(timeWindow)
            println(currentTimeBlock)
            println(nextTimeBlock)
        }

        i++
    }

    return flatList
}

fun transformToAccordionRows(flatBusinessHours: List<FlatBusinessHour>): List<AccordionModel.Row> {
    val rows = mutableListOf<AccordionModel.Row>()
    var currentDayOfWeek = ""

    for (flatBusinessHour in flatBusinessHours) {
        val fullDayOfWeek = convertAbbreviationToFullDay(flatBusinessHour.dayOfWeek)
        val accordionDayOfWeekValue =
            if (fullDayOfWeek != currentDayOfWeek) {
                currentDayOfWeek = fullDayOfWeek
                fullDayOfWeek
            } else {
                ""
            }

        val accordionTimeWindowValue =
            "${convertToConventionalTime(flatBusinessHour.startTime)}-${convertToConventionalTime(flatBusinessHour.endTime)}"

        rows.add(
            AccordionModel.Row(
                accordionDayOfWeek = accordionDayOfWeekValue,
                accordionTimeWindow = accordionTimeWindowValue
            )
        )
    }

    return rows
}

fun dayOfWeekToNumber(day: String): Long {
    return when (day.uppercase(Locale.ROOT)) {
        "MONDAY" -> 1
        "TUESDAY" -> 2
        "WEDNESDAY" -> 3
        "THURSDAY" -> 4
        "FRIDAY" -> 5
        "SATURDAY" -> 6
        "SUNDAY" -> 7
        else -> throw IllegalArgumentException("Invalid day of the week: $day")
    }
}

fun timeOfDayToSeconds(time: String): Int {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val localTime = LocalTime.parse(time, formatter)
    return localTime.toSecondOfDay()
}

fun computeTimeDifference(time1: String, day1: String, time2: String, day2: String): Long {
    val day1Number = dayOfWeekToNumber(day1)
    val day2Number = dayOfWeekToNumber(day2)

    val time1Seconds = timeOfDayToSeconds(time1)
    val time2Seconds = timeOfDayToSeconds(time2)

    val totalSeconds1 = (day1Number - 1) * 24 * 3600 + time1Seconds
    val totalSeconds2 = (day2Number - 1) * 24 * 3600 + time2Seconds

    return totalSeconds2 - totalSeconds1
}

fun daysBetween(firstDay: String, secondDay: String): Int {
    val firstDayOfWeek = DayOfWeek.valueOf(firstDay.uppercase(Locale.ROOT))
    val secondDayOfWeek = DayOfWeek.valueOf(secondDay.uppercase(Locale.ROOT))

    val daysElapsed = secondDayOfWeek.ordinal - firstDayOfWeek.ordinal
    return if (daysElapsed >= 0) daysElapsed else daysElapsed + 7
}

fun findNextTimeWindow(
    flatBusinessHours: List<FlatBusinessHour>,
    inputTimeString: String,
    inputDayOfWeek: String
): String? {
    val inputTime = LocalTime.parse(inputTimeString, DateTimeFormatter.ofPattern("HH:mm:ss"))
    val inputDay = inputDayOfWeek.uppercase(Locale.ROOT)

    // Find the index of the input day of the week
    val inputDayIndex = DayOfWeek.valueOf(inputDay).ordinal

    for (i in 0 until 2) {

        for ((index, timeWindow) in flatBusinessHours.withIndex()) {
            val startTime =
                LocalTime.parse(timeWindow.startTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val endTime =
                LocalTime.parse(timeWindow.endTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
            val dayOfWeekEnum =
                DayOfWeek.valueOf(convertAbbreviationToAllCaps(timeWindow.dayOfWeek))
            val endDay = if (timeWindow.endTimeNextDay) dayOfWeekEnum.plus(1) else dayOfWeekEnum

            val difference1 =
                kotlin.math.abs(
                    computeTimeDifference(
                        inputTimeString,
                        inputDay,
                        flatBusinessHours[(index + 1) % flatBusinessHours.size].startTime,
                        convertAbbreviationToAllCaps(
                            flatBusinessHours[(index + 1) % flatBusinessHours.size].dayOfWeek
                        )
                    )
                )

            if (i == 0) {

                if (timeWindow.endTimeNextDay) {
                    if (
                        ((inputTime.equals(startTime) || inputTime.isAfter(startTime)) &&
                            dayOfWeekEnum == DayOfWeek.valueOf(inputDay)) ||
                            (inputTime.isBefore(endTime) && endDay == DayOfWeek.valueOf(inputDay))
                    ) {
                        println(
                            "currently in a time window, next time window in $timeWindow.daysUntilNextTimeWindow day"
                        )
                        if (timeWindow.daysUntilNextTimeWindow == 0) {
                            return "Open until ${convertToConventionalTime(timeWindow.endTime)}"
                        } else if (timeWindow.daysUntilNextTimeWindow > 0) {
                            return if (difference1 > 86400) {
                                "Open until ${convertToConventionalTime(timeWindow.endTime)}, reopens at ${convertAbbreviationToFullDay(timeWindow.dayOfWeek)}, ${convertToConventionalTime(flatBusinessHours[(index + 1) % flatBusinessHours.size].startTime)}"
                            } else {
                                "Open until ${convertToConventionalTime(timeWindow.endTime)}, reopens at ${convertToConventionalTime(flatBusinessHours[(index + 1) % flatBusinessHours.size].startTime)}"
                            }
                        }
                    }
                } else {
                    if (
                        ((inputTime.equals(startTime) || inputTime.isAfter(startTime)) &&
                            dayOfWeekEnum == DayOfWeek.valueOf(inputDay)) &&
                            (inputTime.isBefore(endTime) && endDay == DayOfWeek.valueOf(inputDay))
                    ) {
                        println(
                            "currently in a time window, next time window in $timeWindow.daysUntilNextTimeWindow day"
                        )
                        if (timeWindow.daysUntilNextTimeWindow == 0) {
                            return "Open until ${convertToConventionalTime(timeWindow.endTime)}"
                        } else if (timeWindow.daysUntilNextTimeWindow > 0) {
                            return if (difference1 > 86400) {
                                "Open until ${convertToConventionalTime(timeWindow.endTime)}, reopens at ${convertAbbreviationToFullDay(timeWindow.dayOfWeek)}, ${convertToConventionalTime(flatBusinessHours[(index + 1) % flatBusinessHours.size].startTime)}"
                            } else {
                                "Open until ${convertToConventionalTime(timeWindow.endTime)}, reopens at ${convertToConventionalTime(flatBusinessHours[(index + 1) % flatBusinessHours.size].startTime)}"
                            }
                        }
                    }
                }
            }
            if (i == 1) {
                return if (difference1 > 86400) {
                    "Opens ${convertAbbreviationToFullDay(timeWindow.dayOfWeek)}, ${convertToConventionalTime(timeWindow.startTime)}"
                } else {
                    "Opens again at ${convertToConventionalTime(timeWindow.startTime)}"
                }
            }
        }
    }

    return null
}
