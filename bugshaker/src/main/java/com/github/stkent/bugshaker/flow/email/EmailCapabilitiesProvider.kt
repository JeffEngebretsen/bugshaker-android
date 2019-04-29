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

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.text.TextUtils
import com.github.stkent.bugshaker.utilities.Logger
import java.util.ArrayList

class EmailCapabilitiesProvider(
        private val packageManager: PackageManager,
        private val genericEmailIntentProvider: GenericEmailIntentProvider,
        private val logger: Logger) {

    private val emailAppList: List<ResolveInfo>
        get() {
            val queryIntent = genericEmailIntentProvider.getEmailIntent(
                    DUMMY_EMAIL_ADDRESSES, DUMMY_EMAIL_SUBJECT_LINE, DUMMY_EMAIL_BODY)

            return packageManager.queryIntentActivities(
                    queryIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

    private val emailWithAttachmentAppList: List<ResolveInfo>
        get() {
            val queryIntent = genericEmailIntentProvider.getEmailWithAttachmentIntent(
                    DUMMY_EMAIL_ADDRESSES, DUMMY_EMAIL_SUBJECT_LINE, DUMMY_EMAIL_BODY, DUMMY_EMAIL_URI)

            return packageManager.queryIntentActivities(
                    queryIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

    fun canSendEmails(): Boolean {
        logger.d("Checking for email apps...")

        val emailAppInfoList = emailAppList

        if (emailAppInfoList.isEmpty()) {
            logger.d("No email apps found.")
            return false
        }

        logEmailAppNames("Available email apps: ", emailAppInfoList)
        return true
    }

    /* default */ internal fun canSendEmailsWithAttachments(): Boolean {
        logger.d("Checking for email apps that can send attachments...")

        val emailAppInfoList = emailWithAttachmentAppList

        if (emailAppInfoList.isEmpty()) {
            logger.d("No email apps can send attachments.")
            return false
        }

        logEmailAppNames("Available email apps that can send attachments: ", emailAppInfoList)
        return true
    }

    private fun logEmailAppNames(
            prefix: String,
            emailAppInfoList: List<ResolveInfo>) {

        val emailAppNames = ArrayList<CharSequence>()
        for (emailAppInfo in emailAppInfoList) {
            emailAppNames.add(emailAppInfo.loadLabel(packageManager))
        }

        val emailAppInfoString = TextUtils.join(", ", emailAppNames)
        logger.d(prefix + emailAppInfoString)
    }

    companion object {

        private val DUMMY_EMAIL_ADDRESSES = arrayOf("someone@example.com")
        private val DUMMY_EMAIL_SUBJECT_LINE = "Any Subject Line"
        private val DUMMY_EMAIL_BODY = "Any Body"
        private val DUMMY_EMAIL_URI = Uri.EMPTY
    }

}
