package com.dragon.multisurface

import android.opengl.Matrix

class MVPMatrix {
    companion object {
        const val MVP_INDEX = 0
        const val VP_INDEX = 16
        const val M_INDEX = 32
    }

    private var viewPortWidth: Float = 0f
    private var viewPortHeight: Float = 0f
    private val values = FloatArray(16 * 3)

    val mvpMatrix = values

    fun updateViewport(w: Int, h: Int): MVPMatrix {
        updateViewport(w.toFloat(), h.toFloat())
        return this
    }

    fun updateViewport(w: Float, h: Float): MVPMatrix {
        viewPortWidth = w
        viewPortHeight = h
        val projection = FloatArray(16)
        val viewer = FloatArray(16)
        Matrix.frustumM(projection, 0, 0f, viewPortWidth, 0f, viewPortHeight, 3f, 15f)
        Matrix.setLookAtM(viewer, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(values, VP_INDEX, projection, 0, viewer, 0)
        Matrix.setIdentityM(values, M_INDEX)
        Matrix.multiplyMM(values, MVP_INDEX, values, VP_INDEX, values, M_INDEX)
        return this
    }

    fun scale(scaleX: Float, scaleY: Float): MVPMatrix {
        Matrix.setIdentityM(values, M_INDEX)
        Matrix.scaleM(values, M_INDEX, scaleX, scaleY, 1f)
        Matrix.multiplyMM(values, MVP_INDEX, values, VP_INDEX, values, M_INDEX)
        return this
    }

    fun rotate(degree: Float): MVPMatrix {
        Matrix.setIdentityM(values, M_INDEX)
        Matrix.translateM(values, M_INDEX, viewPortWidth / 2f, viewPortHeight / 2f, 0f)
        Matrix.rotateM(values, M_INDEX, degree, 0f, 0f, 1f)
        Matrix.translateM(values, M_INDEX, -viewPortWidth / 2f, -viewPortHeight / 2f, 0f)
        Matrix.multiplyMM(values, MVP_INDEX, values, VP_INDEX, values, M_INDEX)
        return this
    }
}