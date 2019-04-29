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

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.WindowManager
import com.github.stkent.bugshaker.ActivityReferenceManager
import com.github.stkent.bugshaker.flow.dialog.DialogProvider
import com.github.stkent.bugshaker.flow.dialog.OnCategoryClicked
import com.github.stkent.bugshaker.flow.email.screenshot.ScreenshotProvider
import com.github.stkent.bugshaker.utilities.ActivityUtils
import com.github.stkent.bugshaker.utilities.Logger
import com.github.stkent.bugshaker.utilities.Toaster
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import java.util.ArrayList

class FeedbackEmailFlowManager(
        private val applicationContext: Context,
        private val emailCapabilitiesProvider: EmailCapabilitiesProvider,
        private val toaster: Toaster,
        private val activityReferenceManager: ActivityReferenceManager,
        private val feedbackEmailIntentProvider: FeedbackEmailIntentProvider,
        private val screenshotProvider: ScreenshotProvider,
        private val alertDialogProvider: DialogProvider,
        private val logger: Logger) {

    private var alertDialog: Dialog? = null

    private var emailAddresses: List<Pair<String, List<String>>>? = null
    private var emailSubjectLine: String? = null
    private var ignoreFlagSecure: Boolean = false

    private val shakeConfirmClickListener = OnClickListener { dialog, which ->
        val activity = activityReferenceManager.validatedActivity ?: return@OnClickListener

        if (emailAddresses!!.size == 1) {
            //skip the category dialog
            composeEmail(activity, emailAddresses!![0].second)
        } else {
            askForBugCategory(activity)
        }
    }

    private val bugCategoryClickListener = object : OnCategoryClicked {
        override fun onClick(category: String) {
            val activity = activityReferenceManager.validatedActivity ?: return
            val emails = ArrayList<String>()
            for ((first, second) in emailAddresses!!) {
                if (first.equals(category)) {
                    emails.addAll(second)
                    break
                }
            }

            composeEmail(activity, emails)
        }
    }

    private val isFeedbackFlowStarted: Boolean
        get() = alertDialog != null && alertDialog!!.isShowing

    private fun askForBugCategory(activity: Activity?) {
        val iter = emailAddresses!!.iterator()
        val categories = ArrayList<String>()
        while (iter.hasNext()) {
            categories.add(iter.next().first)
        }
        alertDialog = alertDialogProvider.getBugCategoryDialog(activity!!, categories, bugCategoryClickListener)
        alertDialog!!.show()
    }

    private fun composeEmail(activity: Activity, emails: List<String>) {
        val emailAddresses = emails.toTypedArray()

        if (shouldAttemptToCaptureScreenshot(activity)) {
            if (emailCapabilitiesProvider.canSendEmailsWithAttachments()) {
                screenshotProvider.getScreenshotUri(activity)
                        .single()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Subscriber<Uri>() {
                            override fun onCompleted() {
                                // This method intentionally left blank.
                            }

                            override fun onError(e: Throwable) {
                                val errorString = "Screenshot capture failed"
                                toaster.toast(errorString)
                                logger.e(errorString)

                                logger.printStackTrace(e)

                                sendEmailWithoutScreenshot(activity, emailAddresses)
                            }

                            override fun onNext(uri: Uri) {
                                sendEmailWithScreenshot(activity, emailAddresses, uri)
                            }
                        })
            } else {
                sendEmailWithoutScreenshot(activity, emailAddresses)
            }
        } else {
            val warningString = "Window is secured; no screenshot taken"

            toaster.toast(warningString)
            logger.d(warningString)

            sendEmailWithoutScreenshot(activity, emailAddresses)
        }
    }

    fun onActivityResumed(activity: Activity) {
        dismissDialog()
        activityReferenceManager.setActivity(activity)
    }

    fun onActivityStopped() {
        dismissDialog()
    }

    fun startFlowIfNeeded(
            emailAddresses: List<Pair<String, List<String>>>,
            emailSubjectLine: String?,
            ignoreFlagSecure: Boolean) {

        if (isFeedbackFlowStarted) {
            logger.d("Feedback flow already started; ignoring shake.")
            return
        }

        this.emailAddresses = emailAddresses
        this.emailSubjectLine = emailSubjectLine
        this.ignoreFlagSecure = ignoreFlagSecure

        showDialog()
    }

    private fun showDialog() {
        val currentActivity = activityReferenceManager.validatedActivity ?: return

        alertDialog = alertDialogProvider.getShakeConfirmAlertDialog(currentActivity, shakeConfirmClickListener)
        alertDialog!!.show()
    }

    private fun dismissDialog() {
        if (alertDialog != null) {
            alertDialog!!.dismiss()
            alertDialog = null
        }
    }

    private fun shouldAttemptToCaptureScreenshot(activity: Activity): Boolean {
        val windowFlags = ActivityUtils.getWindow(activity).attributes.flags

        val isWindowSecured = windowFlags and WindowManager.LayoutParams.FLAG_SECURE == FLAG_SECURE_VALUE

        val result = ignoreFlagSecure || !isWindowSecured

        if (!isWindowSecured) {
            logger.d("Window is not secured; should attempt to capture screenshot.")
        } else {
            if (ignoreFlagSecure) {
                logger.d("Window is secured, but we're ignoring that.")
            } else {
                logger.d("Window is secured, and we're respecting that.")
            }
        }

        return result
    }

    private fun sendEmailWithScreenshot(
            activity: Activity,
            emailAddresses: Array<String>, screenshotUri: Uri) {

        val feedbackEmailIntent = feedbackEmailIntentProvider.getFeedbackEmailIntent(
                emailAddresses,
                emailSubjectLine,
                screenshotUri)

        val resolveInfoList = applicationContext.packageManager
                .queryIntentActivities(feedbackEmailIntent, PackageManager.MATCH_DEFAULT_ONLY)

        for (receivingApplicationInfo in resolveInfoList) {
            // FIXME: revoke these permissions at some point!
            applicationContext.grantUriPermission(
                    receivingApplicationInfo.activityInfo.packageName,
                    screenshotUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        activity.startActivity(feedbackEmailIntent)

        logger.d("Sending email with screenshot.")
    }

    private fun sendEmailWithoutScreenshot(activity: Activity,
                                           emailAddresses: Array<String>) {
        val feedbackEmailIntent = feedbackEmailIntentProvider.getFeedbackEmailIntent(
                emailAddresses,
                emailSubjectLine)

        activity.startActivity(feedbackEmailIntent)

        logger.d("Sending email with no screenshot.")
    }

    companion object {

        private val FLAG_SECURE_VALUE = 0x00002000
    }

}
