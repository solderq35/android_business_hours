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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.businesshours.model.Hour
import com.example.businesshours.ui.theme.BusinessHoursTheme
import com.example.businesshours.R

@Composable
fun HomeScreen(
    businessHoursUiState: BusinessHoursUiState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    when (businessHoursUiState) {
        is BusinessHoursUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
        is BusinessHoursUiState.Success ->
            BusinessHoursGridScreen(
                businessHoursUiState.hours,
                contentPadding = contentPadding,
                modifier = modifier.fillMaxWidth()
            )
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

/** The home screen displaying photo grid. */
@Composable
fun BusinessHoursGridScreen(
    hours: List<Hour>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier.padding(horizontal = 4.dp),
        contentPadding = contentPadding,
    ) {
        items(items = hours, key = { hour -> "${hour.dayOfWeek}_${hour.startLocalTime}" }) { hour ->
            BusinessHoursCard(
                hour,
                modifier = modifier.padding(4.dp).fillMaxWidth().aspectRatio(1.5f)
            )
        }
    }
}

@Composable
fun BusinessHoursCard(hour: Hour, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = hour.dayOfWeek,
            modifier =
                Modifier.padding(16.dp) // Add padding inside the card
                    .fillMaxWidth() // Make the text fill the width of the card
        )
    }
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
        val mockData = List(10) { Hour("$it", "", "") }
        BusinessHoursGridScreen(mockData)
    }
}
