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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

class App(context: Context) {

    val name: String

    val versionName: String

    val versionCode: Int

    val installSource: InstallSource

    init {
        val packageManager = context.packageManager
        val applicationInfo = context.applicationInfo
        val packageInfo = getPackageInfo(context)
        val installerPackageName = packageManager.getInstallerPackageName(context.packageName)

        name = applicationInfo.loadLabel(packageManager).toString()
        versionName = packageInfo?.versionName ?: ""
        versionCode = packageInfo?.versionCode ?: 0
        installSource = InstallSource.fromInstallerPackageName(installerPackageName)
    }

    private fun getPackageInfo(context: Context): PackageInfo? {
        val packageManager = context.packageManager

        return try {
            packageManager.getPackageInfo(context.packageName, 0)
        } catch (ignored: PackageManager.NameNotFoundException) {
            //noinspection ConstantConditions: packageInfo should always be available for the embedding app.
            null
        }

    }

}
