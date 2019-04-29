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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.View
import com.github.stkent.bugshaker.utilities.ActivityUtils
import rx.Observable

internal object NonMapViewsBitmapObservable {

    private val VIEW_PAINT = Paint()

    init {
        VIEW_PAINT.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    }

    fun create(activity: Activity): Observable<Bitmap> {
        return Observable.create { subscriber ->
            try {
                val screenBitmap = getScreenSizedBitmap(activity)

                drawPositionedViewOnScreenBitmap(
                        ActivityUtils.getRootView(activity), screenBitmap)

                subscriber.onNext(screenBitmap)
                subscriber.onCompleted()
            } catch (e: IllegalArgumentException) {
                val exception = InvalidActivitySizeException(e)
                subscriber.onError(exception)
            }
        }
    }/* default */

    private fun drawPositionedViewOnScreenBitmap(
            view: View,
            screenBitmap: Bitmap) {

        val viewBitmap = Bitmap.createBitmap(
                view.width, view.height, Bitmap.Config.ARGB_8888)

        val viewCanvas = Canvas(viewBitmap)
        view.draw(viewCanvas)

        val viewLocationOnScreen = intArrayOf(0, 0)
        view.getLocationOnScreen(viewLocationOnScreen)

        val screenCanvas = Canvas(screenBitmap)
        screenCanvas.drawBitmap(
                viewBitmap,
                viewLocationOnScreen[0].toFloat(),
                viewLocationOnScreen[1].toFloat(),
                VIEW_PAINT)
    }

    private fun getScreenSizedBitmap(activity: Activity): Bitmap {
        val window = ActivityUtils.getWindow(activity)
        val screen = window.windowManager.defaultDisplay
        val screenSize = Point()
        screen.getSize(screenSize)

        return Bitmap.createBitmap(screenSize.x, screenSize.y, Bitmap.Config.ARGB_8888)
    }

}
