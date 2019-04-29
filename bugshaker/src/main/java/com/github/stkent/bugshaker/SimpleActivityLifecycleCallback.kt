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
import android.app.Application
import android.os.Bundle

internal abstract class SimpleActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // This method intentionally left blank
    }

    override fun onActivityResumed(activity: Activity) {
        // This method intentionally left blank
    }

    override fun onActivityStarted(activity: Activity) {
        // This method intentionally left blank
    }

    override fun onActivityPaused(activity: Activity) {
        // This method intentionally left blank
    }

    override fun onActivityStopped(activity: Activity) {
        // This method intentionally left blank
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // This method intentionally left blank
    }

    override fun onActivityDestroyed(activity: Activity) {
        // This method intentionally left blank
    }

}
