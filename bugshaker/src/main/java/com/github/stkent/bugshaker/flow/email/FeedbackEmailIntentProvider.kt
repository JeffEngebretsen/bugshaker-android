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
import android.content.Intent
import android.net.Uri

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FeedbackEmailIntentProvider(
        context: Context,
        private val genericEmailIntentProvider: GenericEmailIntentProvider) {

    private val app: App

    private val environment: Environment

    private val device: Device

    init {
        this.app = App(context)
        this.environment = Environment()
        this.device = Device(context)
    }

    internal fun getFeedbackEmailIntent(
            emailAddresses: Array<String>,
            userProvidedEmailSubjectLine: String?):
            /* default */ Intent {

        val emailSubjectLine = getEmailSubjectLine(userProvidedEmailSubjectLine)
        val emailBody = getApplicationInfoString(app, environment, device)

        return genericEmailIntentProvider
                .getEmailIntent(emailAddresses, emailSubjectLine, emailBody)
    }

    internal fun getFeedbackEmailIntent(
            emailAddresses: Array<String>,
            userProvidedEmailSubjectLine: String?,
            screenshotUri: Uri):
            /* default */ Intent {

        val emailSubjectLine = getEmailSubjectLine(userProvidedEmailSubjectLine)
        val emailBody = getApplicationInfoString(app, environment, device)

        return genericEmailIntentProvider
                .getEmailWithAttachmentIntent(
                        emailAddresses, emailSubjectLine, emailBody, screenshotUri)
    }

    private fun getApplicationInfoString(
            app: App,
            environment: Environment,
            device: Device): String {

        val androidVersionString = String.format(
                "%s (%s)", environment.androidVersionName, environment.androidVersionCode)

        val appVersionString = String.format("%s (%s)", app.versionName, app.versionCode)

        // @formatter:off
        return ("Time Stamp: " + getCurrentUtcTimeStringForDate(Date()) + "\n"
                + "App Version: " + appVersionString + "\n"
                + "Install Source: " + app.installSource + "\n"
                + "Android Version: " + androidVersionString + "\n"
                + "Device Manufacturer: " + device.manufacturer + "\n"
                + "Device Model: " + device.model + "\n"
                + "Display Resolution: " + device.resolution + "\n"
                + "Display Density (Actual): " + device.actualDensity + "\n"
                + "Display Density (Bucket) " + device.densityBucket + "\n"
                + "---------------------\n\n")
        // @formatter:on
    }

    private fun getEmailSubjectLine(userProvidedEmailSubjectLine: String?): String {
        return userProvidedEmailSubjectLine ?: app.name + DEFAULT_EMAIL_SUBJECT_LINE_SUFFIX

    }

    private fun getCurrentUtcTimeStringForDate(date: Date): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z", Locale.getDefault())

        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        return simpleDateFormat.format(date)
    }

    companion object {

        private val DEFAULT_EMAIL_SUBJECT_LINE_SUFFIX = " Android App Feedback"
    }

}
