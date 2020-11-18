package com.dragon.multisurface.extension

import android.graphics.Matrix
import android.graphics.RectF

/**
 * @author dragon
 */

fun RectF.centerCropRect(displayRectF: RectF): RectF {
    val tempMatrix = Matrix()
    tempMatrix.setRectToRect(displayRectF, this, Matrix.ScaleToFit.CENTER)
    tempMatrix.mapRect(displayRectF)
    val scaleX = width() / displayRectF.width()
    val scaleY = height() / displayRectF.height()
    val scale = scaleX.coerceAtLeast(scaleY)
    tempMatrix.reset()
    tempMatrix.setScale(scale, scale, displayRectF.centerX(), displayRectF.centerY())
    tempMatrix.mapRect(displayRectF)
    return displayRectF
}

fun RectF.centerInsideRect(displayRectF: RectF): RectF {
    val tempMatrix = Matrix()
    tempMatrix.setRectToRect(displayRectF, this, Matrix.ScaleToFit.CENTER)
    tempMatrix.mapRect(displayRectF)
    return displayRectF
}