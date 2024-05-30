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

package com.example.businesshours

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.businesshours.ui.BusinessHoursApp
import com.example.businesshours.ui.theme.BusinessHoursTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BusinessHoursTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Image
                        Image(
                            painter = painterResource(id = R.drawable.background),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Your App Content with Translucency
                        Box(
                            modifier =
                                Modifier.fillMaxSize()
                                    .graphicsLayer(alpha = 0.9f) // Adjust alpha for translucency
                        ) {
                            BusinessHoursApp()
                        }
                    }
                }
            }
        }
    }
}
