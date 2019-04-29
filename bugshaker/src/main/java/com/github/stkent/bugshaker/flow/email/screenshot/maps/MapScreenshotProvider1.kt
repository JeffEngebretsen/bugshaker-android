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
package com.github.stkent.bugshaker.flow.email.screenshot.maps

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.support.annotation.VisibleForTesting
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.github.stkent.bugshaker.flow.email.screenshot.BaseScreenshotProvider
import com.github.stkent.bugshaker.utilities.ActivityUtils
import com.github.stkent.bugshaker.utilities.Logger
import com.google.android.gms.maps.MapView
import rx.Observable
import rx.functions.Func2
import java.util.ArrayList
import java.util.LinkedList

class MapScreenshotProvider(
        applicationContext: Context,
        logger: Logger) : BaseScreenshotProvider(applicationContext, logger) {

    override fun getScreenshotBitmap(activity: Activity): Observable<Bitmap> {
        val nonMapViewsBitmapObservable = getNonMapViewsBitmap(activity)

        val rootView = ActivityUtils.getRootView(activity)
        val mapViews = locateMapViewsInHierarchy(rootView)

        if (mapViews.isEmpty()) {
            return nonMapViewsBitmapObservable
        } else {
            val mapViewBitmapsObservable = getMapViewBitmapsObservable(mapViews)

            return Observable
                    .zip(nonMapViewsBitmapObservable, mapViewBitmapsObservable, BITMAP_COMBINING_FUNCTION)
        }
    }

    private fun getMapViewBitmapsObservable(mapViews: List<MapView>): Observable<List<LocatedBitmap>> {
        return Observable
                .from(mapViews)
                .concatMap { mapView -> MapBitmapObservable.create(mapView) }
                .toList()
    }

    @VisibleForTesting
    fun locateMapViewsInHierarchy(view: View): List<MapView> {
        val result = ArrayList<MapView>()

        val viewsToProcess = LinkedList<View>()
        viewsToProcess.add(view)

        while (!viewsToProcess.isEmpty()) {
            val viewToProcess = viewsToProcess.remove()

            if (viewToProcess is MapView && viewToProcess.getVisibility() == VISIBLE) {
                result.add(viewToProcess)
            } else if (viewToProcess is ViewGroup) {

                for (childIndex in 0 until viewToProcess.childCount) {
                    viewsToProcess.add(viewToProcess.getChildAt(childIndex))
                }
            }
        }

        return result
    }

    companion object {

        private val BITMAP_COMBINING_FUNCTION = Func2<Bitmap, List<LocatedBitmap>, Bitmap> { baseLocatedBitmap, overlayLocatedBitmaps ->
            val canvas = Canvas(baseLocatedBitmap)

            for (locatedBitmap in overlayLocatedBitmaps) {
                val overlayLocation = locatedBitmap.location

                canvas.drawBitmap(
                        locatedBitmap.bitmap,
                        overlayLocation[0].toFloat(),
                        overlayLocation[1].toFloat(),
                        MAP_PAINT)
            }

            baseLocatedBitmap
        }

        private val MAP_PAINT = Paint()

        init {
            MAP_PAINT.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
        }
    }

}
