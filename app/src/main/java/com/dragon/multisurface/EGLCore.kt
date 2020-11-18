package com.dragon.multisurface

import android.graphics.RectF
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLSurface
import android.util.Log
import android.util.SparseArray
import android.view.Surface
import androidx.core.util.forEach


class EGLCore {
    private var mEGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext = EGL14.EGL_NO_CONTEXT
    private var mConfigs: EGLConfig? = null
    private val mEGLSurfaces = SparseArray<EGLSurfaceHolder>()

    init {
        log("initEGLContext")
        releaseEGLContext()
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        check(!(mEGLDisplay === EGL14.EGL_NO_DISPLAY)) { "No EGL14 display" }
        val version = IntArray(2)
        check(EGL14.eglInitialize(mEGLDisplay, version,  /*offset*/0, version,  /*offset*/1)) { "Cannot initialize EGL14" }
        val attributeList = intArrayOf(
            EGL14.EGL_RED_SIZE, EGL_COLOR_BIT_LENGTH,
            EGL14.EGL_GREEN_SIZE, EGL_COLOR_BIT_LENGTH,
            EGL14.EGL_BLUE_SIZE, EGL_COLOR_BIT_LENGTH,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT or EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(
            mEGLDisplay,
            attributeList,  /*offset*/0,
            configs,  /*offset*/0, configs.size,
            numConfigs,  /*offset*/0
        )
        checkEglError("eglCreateContext RGB888+recordable ES2")
        mConfigs = configs[0]
        val contextAttributeList = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, GLES_VERSION,
            EGL14.EGL_NONE
        )
        mEGLContext = EGL14.eglCreateContext(
            mEGLDisplay,
            configs[0],
            EGL14.EGL_NO_CONTEXT,
            contextAttributeList,  /*offset*/0
        )
        checkEglError("eglCreateContext")
        check(!(mEGLContext === EGL14.EGL_NO_CONTEXT)) { "No EGLContext could be made" }
        EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, mEGLContext)
    }

    fun addSurfaceHolder(eglSurfaceHolder: EGLSurfaceHolder?) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            log("addSurfaceHolder mEGLDisplay == EGL14.EGL_NO_DISPLAY")
            return
        }
        eglSurfaceHolder ?: return
        check(eglSurfaceHolder.surface.isValid) { "addSurface surface is not valid!!" }
        val surfaceAttribute = intArrayOf(
            EGL14.EGL_NONE
        )
        eglSurfaceHolder.eglSurface = EGL14.eglCreateWindowSurface(
            mEGLDisplay,
            mConfigs,
            eglSurfaceHolder.surface,
            surfaceAttribute,  /*offset*/0
        )
        checkEglError("eglCreateWindowSurface")
        mEGLSurfaces.put(eglSurfaceHolder.surface.hashCode(), eglSurfaceHolder)
    }

    fun removeSurfaceHolder(eglSurfaceHolder: EGLSurfaceHolder?) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            log("removeSurfaceHolder mEGLDisplay == EGL14.EGL_NO_DISPLAY")
            return
        }
        eglSurfaceHolder ?: return
        mEGLSurfaces[eglSurfaceHolder.surface.hashCode()]?.let {
            EGL14.eglDestroySurface(mEGLDisplay, it.eglSurface)
            it.eglSurface = null
            mEGLSurfaces.remove(it.surface.hashCode())
        }
    }
    
    fun removeSurfaceHolder(surface: Surface?) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            log("removeSurfaceHolder mEGLDisplay == EGL14.EGL_NO_DISPLAY")
            return
        }
        surface ?: return
        mEGLSurfaces[surface.hashCode()]?.let {
            EGL14.eglDestroySurface(mEGLDisplay, it.eglSurface)
            it.eglSurface = null
            mEGLSurfaces.remove(surface.hashCode())
        }
    }

    fun render(block: (EGLSurfaceHolder) -> Unit) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            log("render mEGLDisplay == EGL14.EGL_NO_DISPLAY")
            return
        }
        mEGLSurfaces.forEach { _, holder ->
            if (!holder.available) {
                return@forEach
            }
            EGL14.eglMakeCurrent(mEGLDisplay, holder.eglSurface, holder.eglSurface, mEGLContext)
            checkEglError("makeCurrent")
            block.invoke(holder)
            val result = EGL14.eglSwapBuffers(mEGLDisplay, holder.eglSurface)
            when (val error = EGL14.eglGetError()) {
                EGL14.EGL_SUCCESS -> result
                EGL14.EGL_BAD_NATIVE_WINDOW, EGL14.EGL_BAD_SURFACE -> throw IllegalStateException(
                    "swapBuffers: EGL error: 0x" + Integer.toHexString(error)
                )
                else -> throw IllegalStateException(
                    "swapBuffers: EGL error: 0x" + Integer.toHexString(error)
                )
            }
        }
    }

    fun releaseEGLContext() {
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            log("releaseEGLContext")
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            mEGLSurfaces.forEach { _, holder ->
                if (holder.eglSurface != null) {
                    EGL14.eglDestroySurface(mEGLDisplay, holder.eglSurface)
                    holder.eglSurface = null
                }
            }
            mEGLSurfaces.clear()
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }
        mConfigs = null
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
    }

    private fun checkEglError(msg: String) {
        val error = EGL14.eglGetError()
        check(error == EGL14.EGL_SUCCESS) { msg + ": EGL error: 0x" + Integer.toHexString(error) }
    }

    private fun log(msg: String) {
        Log.d("EGLCore", msg)
    }

    class EGLSurfaceHolder(
        val surface: Surface,
        val width: Float,
        val height: Float
    ) {
        var eglSurface: EGLSurface? = null
        var available = true
        val mvpMatrix = MVPMatrix().updateViewport(width, height)
        val viewPortRectF = RectF(0f, 0f, width, height)
    }

    companion object {
        private const val GLES_VERSION = 2
        private const val EGL_COLOR_BIT_LENGTH = 8
        const val EGL_RECORDABLE_ANDROID = 0x3142 // from EGL/eglext.h
    }
}