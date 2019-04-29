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
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import com.github.stkent.bugshaker.flow.dialog.AppCompatDialogProvider
import com.github.stkent.bugshaker.flow.dialog.DialogProvider
import com.github.stkent.bugshaker.flow.email.EmailCapabilitiesProvider
import com.github.stkent.bugshaker.flow.email.FeedbackEmailFlowManager
import com.github.stkent.bugshaker.flow.email.FeedbackEmailIntentProvider
import com.github.stkent.bugshaker.flow.email.GenericEmailIntentProvider
import com.github.stkent.bugshaker.flow.email.screenshot.BasicScreenShotProvider
import com.github.stkent.bugshaker.flow.email.screenshot.ScreenshotProvider
import com.github.stkent.bugshaker.flow.email.screenshot.maps.MapScreenshotProvider
import com.github.stkent.bugshaker.utilities.Logger
import com.github.stkent.bugshaker.utilities.Toaster
import com.squareup.seismic.ShakeDetector

/**
 * The main interaction point for library users. Encapsulates all shake detection. Setters allow users to customize some
 * aspects (recipients, subject line) of bug report emails.
 */
object BugShaker : ShakeDetector.Listener {
    private lateinit var application: Application
    private val RECONFIGURATION_EXCEPTION_MESSAGE = "Configuration must be completed before calling assemble or start"
    private var emailCapabilitiesProvider: EmailCapabilitiesProvider? = null
    private var feedbackEmailFlowManager: FeedbackEmailFlowManager? = null
    private var logger: Logger? = null

    // Instance configuration:
    private var emailAddresses: List<Pair<String, List<String>>>? = null
    private var emailSubjectLine: String? = null
    private var ignoreFlagSecure = false
    private var loggingEnabled = false

    // Instance configuration state:
    private var assembled = false
    private var startAttempted = false

    private val simpleActivityLifecycleCallback = object : SimpleActivityLifecycleCallback() {
        override fun onActivityResumed(activity: Activity) {
            feedbackEmailFlowManager!!.onActivityResumed(activity)
        }

        override fun onActivityStopped(activity: Activity) {
            feedbackEmailFlowManager!!.onActivityStopped()
        }
    }

    /**
     * @return a MapScreenshotProvider if the embedding application utilizes the Google Maps Android API, and a
     * BasicScreenshotProvider otherwise
     */
    private val screenshotProvider: ScreenshotProvider
        get() {
            try {
                Class.forName(
                        "com.google.android.gms.maps.GoogleMap",
                        false,
                        BugShaker::class.java!!.getClassLoader())

                logger!!.d("Detected that embedding app includes Google Maps as a dependency.")

                return MapScreenshotProvider(application, logger!!)
            } catch (e: ClassNotFoundException) {
                logger!!.d("Detected that embedding app does not include Google Maps as a dependency.")

                return BasicScreenShotProvider(application, logger!!)
            }

        }

    private val alertDialogProvider: DialogProvider
        get() {
            return AppCompatDialogProvider()
        }

    fun withApplication(application: Application): BugShaker {
        BugShaker.application = application
        return BugShaker
    }

    /**
     * (Required) Defines one or more email addresses to send bug reports to. This method MUST be called before calling
     * `assemble`. This method CANNOT be called after calling `assemble` or `start`.
     *
     * @param emailsByCategory A list of category email list pairs for the user to pick from
     * @param otherEmails one or more email addresses for no category
     * @return the current `BugShaker` instance (to allow for method chaining)
     */
    fun setEmailAddresses(emailsByCategory: List<Pair<String, List<String>>>): BugShaker {
        if (assembled || startAttempted) {
            throw IllegalStateException(
                    "Configuration must be complete before calling assemble or start")
        }

        this.emailAddresses = emailsByCategory
        return this
    }

    /**
     * (Optional) Defines a custom subject line to use for all bug reports. By default, reports will use the string
     * defined in `DEFAULT_SUBJECT_LINE`. This method CANNOT be called after calling `assemble` or
     * `start`.
     *
     * @param emailSubjectLine a custom email subject line
     * @return the current `BugShaker` instance (to allow for method chaining)
     */
    fun setEmailSubjectLine(emailSubjectLine: String): BugShaker {
        if (assembled || startAttempted) {
            throw IllegalStateException(RECONFIGURATION_EXCEPTION_MESSAGE)
        }

        this.emailSubjectLine = emailSubjectLine
        return this
    }

    /**
     * (Optional) Enables debug and error log messages. Logging is disabled by default. This method CANNOT be called
     * after calling `assemble` or `start`.
     *
     * @param loggingEnabled true if logging should be enabled; false otherwise
     * @return the current `BugShaker` instance (to allow for method chaining)
     */
    fun setLoggingEnabled(loggingEnabled: Boolean): BugShaker {
        if (assembled || startAttempted) {
            throw IllegalStateException(RECONFIGURATION_EXCEPTION_MESSAGE)
        }

        this.loggingEnabled = loggingEnabled
        return this
    }

    /**
     * (Optional) Choose whether to ignore the `FLAG_SECURE` `Window` flag when capturing
     * screenshots. This method CANNOT be called after calling `assemble` or `start`.
     *
     * @param ignoreFlagSecure true if screenshots should be allowed even when `FLAG_SECURE` is set on the
     * current `Window`; false otherwise
     * @return the current `BugShaker` instance (to allow for method chaining)
     */
    fun setIgnoreFlagSecure(ignoreFlagSecure: Boolean): BugShaker {
        if (assembled || startAttempted) {
            throw IllegalStateException(RECONFIGURATION_EXCEPTION_MESSAGE)
        }

        this.ignoreFlagSecure = ignoreFlagSecure
        return this
    }

    /**
     * (Required) Assembles dependencies based on provided configuration information. This method CANNOT be called more
     * than once. This method CANNOT be called after calling `start`.
     *
     * @return the current `BugShaker` instance (to allow for method chaining)
     */
    fun assemble(): BugShaker {
        if (assembled) {
            logger!!.d("You have already assembled this BugShaker instance. Calling assemble again is a no-op.")
            return this
        }

        if (startAttempted) {
            throw IllegalStateException("You can only call assemble before calling start.")
        }

        logger = Logger(loggingEnabled)

        val genericEmailIntentProvider = GenericEmailIntentProvider()

        emailCapabilitiesProvider = EmailCapabilitiesProvider(
                application.packageManager,
                genericEmailIntentProvider,
                logger!!)

        feedbackEmailFlowManager = FeedbackEmailFlowManager(
                application,
                emailCapabilitiesProvider!!,
                Toaster(application),
                ActivityReferenceManager(),
                FeedbackEmailIntentProvider(application, genericEmailIntentProvider),
                screenshotProvider,
                alertDialogProvider,
                logger!!)

        assembled = true
        return this
    }

    /**
     * (Required) Start listening for device shaking. You MUST call `assemble` before calling this method.
     */
    fun start() {
        if (!assembled) {
            throw IllegalStateException("You MUST call assemble before calling start.")
        }

        if (startAttempted) {
            logger!!.d("You have already attempted to start this BugShaker instance. Calling start " + "again is a no-op.")

            return
        }

        if (emailCapabilitiesProvider!!.canSendEmails()) {
            application.registerActivityLifecycleCallbacks(simpleActivityLifecycleCallback)

            val sensorManager = application.getSystemService(SENSOR_SERVICE) as SensorManager
            val shakeDetector = ShakeDetector(this)

            val didStart = shakeDetector.start(sensorManager)

            if (didStart) {
                logger!!.d("Shake detection successfully started!")
            } else {
                logger!!.e("Error starting shake detection: hardware does not support detection.")
            }
        } else {
            logger!!.e("Error starting shake detection: device cannot send emails.")
        }

        startAttempted = true
    }

    override fun hearShake() {
        logger!!.d("Shake detected!")

        feedbackEmailFlowManager!!.startFlowIfNeeded(
                emailAddresses!!,
                emailSubjectLine,
                ignoreFlagSecure)
    }
}
