package com.dragon.multisurface

import android.graphics.BitmapFactory
import android.graphics.RectF
import android.opengl.GLES20
import android.os.Bundle
import android.os.Handler
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.dragon.multisurface.background.RenderScope
import com.dragon.multisurface.extension.MirrorType
import com.dragon.multisurface.extension.assignPosition
import com.dragon.multisurface.extension.assignTextureCoordinate
import com.dragon.multisurface.extension.centerInsideRect
import com.dragon.multisurface.program.TextureProgram
import com.dragon.multisurface.texture.BitmapTexture
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var eglSurfaceHolder1: EGLCore.EGLSurfaceHolder? = null
    var eglSurfaceHolder2: EGLCore.EGLSurfaceHolder? = null
    var offsetX = 0f
    lateinit var renderScope :RenderScope
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        renderScope = RenderScope(object : RenderScope.Render{
            lateinit var program:TextureProgram
            lateinit var texture:BitmapTexture
            val position = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
            val textureCoordinate = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
            val viewPortRectF = RectF()
            val displayRectF = RectF()
            override fun onCreate() {
                program = TextureProgram()
                texture = BitmapTexture(BitmapFactory.decodeStream(this@MainActivity.resources.assets.open("google.png")), 0f, mirrorType = MirrorType.VERTICAL)
            }

            override fun onDestroy() {
                program.release()
            }

            override fun onDrawFrame(eglSurfaceHolder: EGLCore.EGLSurfaceHolder) {
                viewPortRectF.set(0f, 0f, eglSurfaceHolder.width, eglSurfaceHolder.height)
                displayRectF.set(0f, 0f, eglSurfaceHolder.width / 2, eglSurfaceHolder.height / 2)

                viewPortRectF.centerInsideRect(displayRectF)

                if (offsetX > displayRectF.width() / 2) {
                    offsetX = -displayRectF.width() / 2
                }
                displayRectF.offset(offsetX, 0f)
                textureCoordinate.assignTextureCoordinate(
                    eglSurfaceHolder.width / 2,
                    eglSurfaceHolder.height / 2,
                    texture.width.toFloat(),
                    texture.height.toFloat(),
                    mirrorType = MirrorType.VERTICAL
                )
                position.assignPosition(displayRectF, viewPortRectF.height())

                GLES20.glViewport(0, 0, viewPortRectF.width().toInt(), viewPortRectF.height().toInt())
                GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                program.draw(
                    texture.textureId,
                    position,
                    textureCoordinate,
                    eglSurfaceHolder.mvpMatrix.mvpMatrix
                )
            }

        })
        surfaceView1.holder.addCallback(object:SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder?) {

            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                if (eglSurfaceHolder1 == null) {
                    eglSurfaceHolder1 = EGLCore.EGLSurfaceHolder(holder!!.surface, width.toFloat(), height.toFloat())
                    renderScope.addSurfaceHolder(eglSurfaceHolder1)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                renderScope.removeSurfaceHolder(eglSurfaceHolder1)
                eglSurfaceHolder1 = null
            }
        })

        surfaceView2.holder.addCallback(object:SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder?) {
            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                if (eglSurfaceHolder2 == null) {
                    eglSurfaceHolder2 = EGLCore.EGLSurfaceHolder(holder!!.surface, width.toFloat(), height.toFloat())
                    renderScope.addSurfaceHolder(eglSurfaceHolder2)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                renderScope.removeSurfaceHolder(eglSurfaceHolder2)
                eglSurfaceHolder2 = null
            }
        })
        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        renderScope.release()
    }
    private fun refresh() {
        renderScope.requestRender()
        offsetX += 2
        if (lifecycle.currentState >= Lifecycle.State.DESTROYED) {
            Handler().postDelayed({
                refresh()
            }, 40)
        }
    }
}