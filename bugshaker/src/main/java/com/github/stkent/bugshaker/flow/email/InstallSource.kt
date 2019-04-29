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

/*
 * An attempt to make an Algebraic data type in Java (https://en.wikipedia.org/wiki/Algebraic_data_type). See also
 * https://grundlefleck.github.io/2014/07/17/sealing-algebraic-data-types-in-java.html
 */
abstract class InstallSource private constructor()// This constructor intentionally left blank.
{

    class GooglePlayStoreInstallSource : InstallSource() {
        override fun toString(): String {
            return "Google Play Store"
        }
    }

    class AmazonAppStoreInstallSource : InstallSource() {
        override fun toString(): String {
            return "Amazon Appstore"
        }
    }

    class AmazonUndergroundInstallSource : InstallSource() {
        override fun toString(): String {
            return "Amazon Underground"
        }
    }

    class PackageInstallerInstallSource : InstallSource() {
        override fun toString(): String {
            return "Package Installer"
        }
    }

    class UnknownInstallSource : InstallSource() {
        override fun toString(): String {
            return "Unknown"
        }
    }

    class UnrecognizedInstallSource constructor(private val installerPackageName: String) : InstallSource() {

        override fun toString(): String {
            return installerPackageName
        }

    }

    companion object {

        /**
         * Package name for the Amazon App Store.
         */
        private val AMAZON_APP_STORE_PACKAGE_NAME = "com.amazon.venezia"

        /**
         * Package name for Amazon Underground.
         */
        private val AMAZON_UNDERGROUND_PACKAGE_NAME = "com.amazon.mshop.android"

        /**
         * Package name for the Google Play Store. Value can be verified here:
         * https://developers.google.com/android/reference/com/google/android/gms/common/GooglePlayServicesUtil.html#GOOGLE_PLAY_STORE_PACKAGE
         */
        private val GOOGLE_PLAY_STORE_PACKAGE_NAME = "com.android.vending"

        /**
         * Package name for Google's Package Installer. My guess is that apps installed using the
         * [PackageInstaller](https://developer.android.com/reference/android/content/pm/PackageInstaller.html)
         * APIs will report as having been installed from this source. Since this installer package name result hides the
         * originating app package name from us, a consuming application that *really* needs to know how an app was
         * installed will need to fall back on inspecting the apps currently installed on the device and making an educated
         * guess.
         */
        private val PACKAGE_INSTALLER_PACKAGE_NAME = "com.google.android.packageinstaller"

        internal/* default */ fun fromInstallerPackageName(installerPackageName: String?): InstallSource {
            return when {
                GOOGLE_PLAY_STORE_PACKAGE_NAME.equals(installerPackageName, ignoreCase = true) -> GooglePlayStoreInstallSource()
                AMAZON_APP_STORE_PACKAGE_NAME.equals(installerPackageName, ignoreCase = true) -> AmazonAppStoreInstallSource()
                AMAZON_UNDERGROUND_PACKAGE_NAME.equals(installerPackageName, ignoreCase = true) -> AmazonUndergroundInstallSource()
                PACKAGE_INSTALLER_PACKAGE_NAME.equals(installerPackageName, ignoreCase = true) -> PackageInstallerInstallSource()
                else -> installerPackageName?.let { UnrecognizedInstallSource(it) }
                        ?: UnknownInstallSource()
            }
        }
    }

}
