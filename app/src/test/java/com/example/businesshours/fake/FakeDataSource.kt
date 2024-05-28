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
package com.example.businesshours.fake

import com.example.businesshours.model.BusinessHours

object FakeDataSource {

    private const val dayOne = "TUE"
    private const val dayTwo = "WED"
    private const val startOne = "07:00:00"
    private const val startTwo = "07:00:00"
    private const val endOne = "13:00:00"
    private const val endTwo = "15:00:00"
    val hoursLists =
        listOf(
            BusinessHours(dayOfWeek = dayOne, startLocalTime = startOne, endLocalTime = endOne),
            BusinessHours(dayOfWeek = dayTwo, startLocalTime = startTwo, endLocalTime = endTwo),
        )
}
