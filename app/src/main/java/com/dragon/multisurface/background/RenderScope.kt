package com.dragon.multisurface.background

import android.view.Surface
import com.dragon.multisurface.EGLCore

/**
 * @author dragon
 */
class RenderScope(private val render: Render) : BackgroundScope by HandlerThreadScope() {
    private var eglCore: EGLCore? = null

    init {
        runInBackground {
            eglCore = EGLCore()
            render.onCreate()
        }
    }

    fun addSurfaceHolder(eglSurfaceHolder: EGLCore.EGLSurfaceHolder?) = runInBackground {
        eglCore?.addSurfaceHolder(eglSurfaceHolder)
    }

    fun removeSurfaceHolder(eglSurfaceHolder: EGLCore.EGLSurfaceHolder?) = runInBackground {
        eglCore?.removeSurfaceHolder(eglSurfaceHolder)
    }

    fun removeSurfaceHolder(surface: Surface?) = runInBackground {
        eglCore?.removeSurfaceHolder(surface)
    }

    fun release() = runInBackground {
        render.onDestroy()
        eglCore?.releaseEGLContext()
        eglCore = null
        quit()
    }

    fun requestRender() = runInBackground {
        eglCore?.render { render.onDrawFrame(it) }
    }

    interface Render {
        fun onCreate()
        fun onDestroy()
        fun onDrawFrame(eglSurfaceHolder: EGLCore.EGLSurfaceHolder)
    }
}