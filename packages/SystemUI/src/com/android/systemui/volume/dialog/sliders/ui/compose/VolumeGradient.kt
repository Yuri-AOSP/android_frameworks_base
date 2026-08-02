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

package com.android.systemui.volume.dialog.sliders.ui.compose

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.android.systemui.util.GradientColorUtils

/** The two ends of a volume slider gradient. */
data class VolumeGradient(
    val startColor: Color,
    val endColor: Color,
)

/** Whether the user asked for a gradient on the volume sliders. */
@Composable
fun rememberVolumeGradientEnabled(): Boolean {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    fun readEnabled(): Boolean {
        return try {
            Settings.System.getIntForUser(
                contentResolver, Settings.System.VOLUME_SLIDER_GRADIENT, 0,
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
            Settings.System.getUriFor(Settings.System.VOLUME_SLIDER_GRADIENT),
            false, observer, UserHandle.USER_ALL
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }

    return enabled
}

@Composable
fun rememberVolumeGradientColorMode(enabled: Boolean): Int =
    GradientColorUtils.rememberGradientColorMode(enabled)

@Composable
fun rememberVolumeGradientCustomColors(enabled: Boolean): VolumeGradient {
    val (start, end) = GradientColorUtils.rememberGradientCustomColors(
        enabled,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
    )
    return VolumeGradient(startColor = start, endColor = end)
}

fun createGradientBrush(gradient: VolumeGradient?): Brush? {
    if (gradient == null) return null

    return Brush.verticalGradient(
        colors = listOf(gradient.endColor, gradient.startColor)
    )
}
