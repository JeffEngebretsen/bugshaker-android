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
package com.github.stkent.bugshaker.utilities

import android.util.Log

class Logger(private val loggingEnabled: Boolean) {

    fun d(message: CharSequence) {
        if (loggingEnabled) {
            Log.d(TAG, message.toString())
        }
    }

    fun e(message: CharSequence) {
        if (loggingEnabled) {
            Log.e(TAG, message.toString())
        }
    }

    fun printStackTrace(throwable: Throwable) {
        if (loggingEnabled) {
            Log.e(TAG, "Logging caught exception", throwable)
        }
    }

    companion object {

        private val TAG = "BugShaker-Library"
    }

}
