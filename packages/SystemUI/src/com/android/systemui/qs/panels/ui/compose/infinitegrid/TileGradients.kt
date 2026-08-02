/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.panels.ui.compose.infinitegrid

import android.database.ContentObserver
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.android.systemui.util.GradientColorUtils

/** Whether the user asked for gradient backgrounds on quick settings tiles. */
@Composable
fun rememberQsGradient(): Boolean {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    fun readEnabled(): Boolean {
        return try {
            Settings.System.getIntForUser(
                contentResolver, Settings.System.QS_TILE_GRADIENT, 0,
                UserHandle.USER_CURRENT
            ) != 0
        } catch (_: Throwable) {
            false
        }
    }

    var enabled by remember { mutableStateOf(readEnabled()) }

    DisposableEffect(contentResolver) {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                enabled = readEnabled()
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.QS_TILE_GRADIENT),
            false, observer, UserHandle.USER_ALL
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }

    return enabled
}

/** The brush to paint an active tile with, or null when gradients are off. */
@Composable
fun rememberQsTileBackgroundBrush(): Brush? {
    val enabled = rememberQsGradient()
    val mode = rememberQsGradientColorMode(enabled)
    val (start, end) = rememberQsGradientCustomColors(enabled)

    if (!enabled) return null

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val colors = remember(mode, start, end, primaryColor, secondaryColor) {
        if (mode == 1) {
            listOf(start, end)
        } else {
            listOf(primaryColor, secondaryColor)
        }
    }

    return remember(colors) {
        Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset.Infinite,
        )
    }
}

@Composable
private fun rememberQsGradientColorMode(enabled: Boolean): Int =
    GradientColorUtils.rememberGradientColorMode(enabled)

@Composable
private fun rememberQsGradientCustomColors(enabled: Boolean): Pair<Color, Color> =
    GradientColorUtils.rememberGradientCustomColors(
        enabled,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
    )
