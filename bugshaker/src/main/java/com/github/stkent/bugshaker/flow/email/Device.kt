/*
 * Copyright 2016 Stuart Kent
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.stkent.bugshaker.flow.email

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics

import java.util.Locale.US

class Device(context: Context) {

    val resolution: String

    val actualDensity: String

    val densityBucket: String

    val manufacturer: String
        get() = Build.MANUFACTURER.toUpperCase(US)

    val model: String
        get() = Build.MODEL.toUpperCase(US)

    private fun getDensityBucketString(displayMetrics: DisplayMetrics): String {
        when (displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> return "ldpi"
            DisplayMetrics.DENSITY_MEDIUM -> return "mdpi"
            DisplayMetrics.DENSITY_HIGH -> return "hdpi"
            DisplayMetrics.DENSITY_XHIGH -> return "xhdpi"
            DisplayMetrics.DENSITY_XXHIGH -> return "xxhdpi"
            DisplayMetrics.DENSITY_XXXHIGH -> return "xxxhdpi"
            DisplayMetrics.DENSITY_TV -> return "tvdpi"
            else -> return "Unknown"
        }
    }

    init {
        val displayMetrics = context.resources.displayMetrics
        resolution = displayMetrics.heightPixels.toString() + "x" + displayMetrics.widthPixels
        actualDensity = displayMetrics.densityDpi.toString() + "dpi"
        densityBucket = getDensityBucketString(displayMetrics)
    }

}
