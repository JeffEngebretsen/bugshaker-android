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
import android.support.v7.app.AlertDialog
import android.widget.ArrayAdapter
import com.github.stkent.bugshaker.R

class AppCompatDialogProvider : DialogProvider {

    override fun getShakeConfirmAlertDialog(
            activity: Activity,
            reportBugClickListener: DialogInterface.OnClickListener): Dialog {

        return AlertDialog.Builder(activity)
                .setTitle(DialogProvider.ALERT_DIALOG_TITLE)
                .setMessage(DialogProvider.ALERT_DIALOG_MESSAGE)
                .setPositiveButton(DialogProvider.ALERT_DIALOG_POSITIVE_BUTTON, reportBugClickListener)
                .setNegativeButton(DialogProvider.ALERT_DIALOG_NEGATIVE_BUTTON, null)
                .setCancelable(DialogProvider.ALERT_DIALOG_CANCELABLE)
                .create()
    }

    override fun getBugCategoryDialog(activity: Activity, categories: List<String>, bugCategoryClickListener: OnCategoryClicked): Dialog {
        val adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_single_choice, categories)
        return AlertDialog.Builder(activity)
                .setTitle(R.string.select_category_title)
                .setAdapter(adapter) { dialog, which -> bugCategoryClickListener.onClick(categories[which]) }
                .setNegativeButton(DialogProvider.ALERT_DIALOG_NEGATIVE_BUTTON, null)
                .setCancelable(DialogProvider.ALERT_DIALOG_CANCELABLE)
                .create()
    }
}
