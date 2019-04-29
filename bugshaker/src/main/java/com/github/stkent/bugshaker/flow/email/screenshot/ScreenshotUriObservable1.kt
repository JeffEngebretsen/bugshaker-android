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
package com.github.stkent.bugshaker.flow.email.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.content.FileProvider
import com.github.stkent.bugshaker.utilities.Logger
import rx.Observable
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/* default */ internal object ScreenshotUriObservable {

    private val AUTHORITY_SUFFIX = ".bugshaker.fileprovider"
    private val SCREENSHOTS_DIRECTORY_NAME = "bugshaker-internal"
    private val SCREENSHOT_FILE_NAME = "latest-screenshot.jpg"
    private val JPEG_COMPRESSION_QUALITY = 90

    /* default */  fun create(
            applicationContext: Context,
            bitmap: Bitmap,
            logger: Logger): Observable<Uri> {

        return Observable.create { subscriber ->
            var fileOutputStream: OutputStream? = null

            try {
                val screenshotFile = getScreenshotFile(applicationContext)

                fileOutputStream = BufferedOutputStream(
                        FileOutputStream(screenshotFile))

                bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        JPEG_COMPRESSION_QUALITY,
                        fileOutputStream)

                fileOutputStream.flush()

                logger.d("Screenshot saved to " + screenshotFile.absolutePath)

                val result = FileProvider.getUriForFile(
                        applicationContext,
                        applicationContext.packageName + AUTHORITY_SUFFIX,
                        screenshotFile)

                logger.d("Screenshot Uri created: $result")

                subscriber.onNext(result)
                subscriber.onCompleted()
            } catch (e: IOException) {
                subscriber.onError(e)
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close()
                    } catch (ignored: IOException) {
                        // We did our best...
                        logger.e("Failed to close OutputStream.")
                    }

                }
            }
        }
    }

    private fun getScreenshotFile(applicationContext: Context): File {
        val screenshotsDir = File(applicationContext.filesDir, SCREENSHOTS_DIRECTORY_NAME)


        screenshotsDir.mkdirs()

        return File(screenshotsDir, SCREENSHOT_FILE_NAME)
    }

}// This constructor intentionally left blank.
