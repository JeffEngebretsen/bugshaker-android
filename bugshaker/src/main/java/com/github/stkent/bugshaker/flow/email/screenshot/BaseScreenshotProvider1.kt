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

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.github.stkent.bugshaker.utilities.Logger
import rx.Observable
import rx.schedulers.Schedulers

abstract class BaseScreenshotProvider(
        private val applicationContext: Context,
        private val logger: Logger) : ScreenshotProvider {

    abstract fun getScreenshotBitmap(activity: Activity): Observable<Bitmap>

    override fun getScreenshotUri(activity: Activity): Observable<Uri> {
        return getScreenshotBitmap(activity)
                .observeOn(Schedulers.io())
                .flatMap { bitmap -> ScreenshotUriObservable.create(applicationContext, bitmap, logger) }
    }

    protected fun getNonMapViewsBitmap(activity: Activity): Observable<Bitmap> {
        return NonMapViewsBitmapObservable.create(activity)
    }

}
