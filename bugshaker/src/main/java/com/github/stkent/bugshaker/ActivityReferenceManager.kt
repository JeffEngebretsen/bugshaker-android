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
package com.github.stkent.bugshaker

import android.app.Activity
import android.os.Build

import java.lang.ref.WeakReference

class ActivityReferenceManager {

    private var wActivity: WeakReference<Activity>? = null

    val validatedActivity: Activity?
        get() {
            if (wActivity == null) {
                return null
            }

            val activity = wActivity!!.get()
            return if (!isActivityValid(activity)) {
                null
            } else activity

        }

    fun setActivity(activity: Activity) {
        this.wActivity = WeakReference(activity)
    }

    private fun isActivityValid(activity: Activity?): Boolean {
        if (activity == null) {
            return false
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            !activity.isFinishing && !activity.isDestroyed
        } else {
            !activity.isFinishing
        }
    }

}
