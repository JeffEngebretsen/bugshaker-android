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
package com.github.stkent.bugshaker.flow.dialog

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface

interface DialogProvider {

    fun getShakeConfirmAlertDialog(
            activity: Activity,
            reportBugClickListener: DialogInterface.OnClickListener): Dialog

    fun getBugCategoryDialog(
            activity: Activity, categories: List<String>,
            bugCategoryClickListener: OnCategoryClicked): Dialog

    companion object {

        val ALERT_DIALOG_TITLE = "Shake detected!"
        val ALERT_DIALOG_MESSAGE = "Would you like to report a bug?"
        val ALERT_DIALOG_POSITIVE_BUTTON = "Report"
        val ALERT_DIALOG_NEGATIVE_BUTTON = "Cancel"
        val ALERT_DIALOG_CANCELABLE = false
    }
}
