package com.dragon.multisurface.extension

import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import java.nio.FloatBuffer

val textureCoordinateArray = floatArrayOf(
    0f, 0f,
    1f, 0f,
    0f, 1f,
    1f, 1f
)

val positionArray = floatArrayOf(
    -1f, -1f,
    1f, -1f,
    -1f, 1f,
    1f, 1f
)

fun FloatBuffer.assignPosition(): FloatBuffer {
    clear()
    put(positionArray)
    rewind()
    return this
}

/**
 * x and y is gl coordinate
 */
fun FloatBuffer.assignPosition(x: Float, y: Float, w: Float, h: Float): FloatBuffer {
    rewind()
    put(x).put(y)
    put(x + w).put(y)
    put(x).put(y + h)
    put(x + w).put(y + h)
    rewind()
    return this
}

/**
 * due to the view coordinate is different from gl environment
 *      view top(0)->bottom(viewport height)
 *      gl   top(viewport height) ->bottom(0)
 */
fun FloatBuffer.assignPosition(rectF: RectF, viewPortHeight: Float): FloatBuffer {
    rewind()
    put(rectF.left).put(viewPortHeight - rectF.bottom)
    put(rectF.right).put(viewPortHeight - rectF.bottom)
    put(rectF.left).put(viewPortHeight - rectF.top)
    put(rectF.right).put(viewPortHeight - rectF.top)
    rewind()
    return this
}

fun FloatBuffer.assignTextureCoordinate(
    viewportWidth: Float = 1f,
    viewportHeight: Float = 1f,
    textureWidth: Float = 1f,
    textureHeight: Float = 1f,
    rotate: Float = 0f,
    scaleType: ScaleType = ScaleType.CENTER_INSIDE,
    mirrorType: MirrorType = MirrorType.NONE
): FloatBuffer {
    val viewportRectF = RectF(0f, 0f, viewportWidth, viewportHeight)
    val textureRectF = RectF(0f, 0f, if (rotate.toInt() % 180 == 0) textureWidth else textureHeight, if (rotate.toInt() % 180 == 0) textureHeight else textureWidth)
    val scaleMatrix = Matrix()
    scaleMatrix.setRectToRect(textureRectF, viewportRectF, Matrix.ScaleToFit.CENTER)
    scaleMatrix.mapRect(textureRectF)
    var scaleX = viewportWidth / textureRectF.width()
    var scaleY = viewportHeight / textureRectF.height()

    if (scaleType == ScaleType.CROP_CENTER) {
        val scale = scaleX.coerceAtLeast(scaleY)
        scaleMatrix.reset()
        scaleMatrix.setScale(scale, scale)
        scaleMatrix.mapRect(textureRectF)
        scaleX = viewportWidth / textureRectF.width()
        scaleY = viewportHeight / textureRectF.height()
    }

    val array = FloatArray(8)
    val matrix = Matrix()
    matrix.setRotate(-rotate, 0.5f, 0.5f)
    matrix.preScale(
        if (mirrorType == MirrorType.HORIZONTAL || mirrorType == MirrorType.VERTICAL_AND_HORIZONTAL) -1f * scaleX else scaleX,
        if (mirrorType == MirrorType.VERTICAL || mirrorType == MirrorType.VERTICAL_AND_HORIZONTAL) -1f * scaleY else scaleY,
        0.5f,
        0.5f
    )
    matrix.mapPoints(array, textureCoordinateArray)
    clear()
    put(array)
    rewind()
    return this
}


fun FloatBuffer.dump(tag: String, groupSize: Int = 2) {
    val sb = StringBuilder("\n")
    for (index in position() until limit() step groupSize) {
        sb.append(get(index)).append(",").append(get(index + 1)).append("\n")
    }
    Log.d("FloatBuffer", "$tag $sb")
}

enum class ScaleType {
    CROP_CENTER,
    CENTER_INSIDE
}

enum class MirrorType {
    VERTICAL,
    HORIZONTAL,
    VERTICAL_AND_HORIZONTAL,
    NONE
}
