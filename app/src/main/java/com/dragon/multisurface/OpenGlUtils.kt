package com.dragon.multisurface

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class OpenGlUtils {
    companion object {
        private const val TAG = "OpenGlUtils"
        private const val DEBUG = true
        fun createProgram(vertexShader: String, fragmentShader: String): Int {
            val vertex =
                loadShader(
                    GLES20.GL_VERTEX_SHADER,
                    vertexShader
                )
            if (vertex == 0) {
                Log.d(TAG, "load error vertex:$vertex")
                return 0
            }
            val fragment =
                loadShader(
                    GLES20.GL_FRAGMENT_SHADER,
                    fragmentShader
                )
            if (fragment == 0) {
                Log.d(TAG, "load error fragmentShader:$fragment")
                return 0
            }

            var program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertex)
            GLES20.glAttachShader(program, fragment)
            GLES20.glLinkProgram(program)
            val link = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0)
            if (link[0] <= 0) {
                Log.d(TAG, "Linking Failed")
                Log.d(TAG, "Linking vertex:$vertex")
                Log.d(TAG, "Linking fragmentShader:$fragmentShader")
                GLES20.glDeleteProgram(program)
                program = 0
            }
            GLES20.glDeleteShader(vertex)
            GLES20.glDeleteShader(fragment)
            if (DEBUG) Log.d(TAG, "createProgram program $program")
            return program
        }

        private fun loadShader(type: Int, codeString: String): Int {
            val compiled = intArrayOf(1)
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, codeString)
            GLES20.glCompileShader(shader)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.d(TAG, "Compilation\n" + GLES20.glGetShaderInfoLog(shader))
                return 0
            }
            return shader
        }

        fun destroyProgram(program: Int) {
            if (DEBUG) Log.d(
                TAG, "destroyProgram program $program")
            GLES20.glDeleteProgram(program)
        }

        fun createBitmapTexture(bitmap: Bitmap): Int {
            val textureArray = IntArray(1)
            GLES20.glGenTextures(1, textureArray, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureArray[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            if (DEBUG) Log.d(TAG, "createBitmapTexture textureId ${textureArray[0]}")
            return textureArray[0]
        }

        fun createTexture(width: Int, height: Int): Int {
            val textureArray = intArrayOf(1)
            GLES20.glGenTextures(1, textureArray, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureArray[0])
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                width,
                height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            if (DEBUG) Log.d(
                TAG, "createTexture textureId ${textureArray[0]}")
            return textureArray[0]
        }

        fun createTexture(): Int {
            val textureArray = intArrayOf(1)
            GLES20.glGenTextures(1, textureArray, 0)
            if (DEBUG) Log.d(
                TAG, "createTexture textureId ${textureArray[0]}")
            return textureArray[0]
        }

        fun releaseTexture(textureId: Int) {
            if (DEBUG) Log.d(
                TAG, "releaseTexture textureId $textureId")
            GLES20.glDeleteTextures(1, IntArray(1) { textureId }, 0)
        }

        fun createFrameBuffer(textureId: Int): Int {
            val frameBufferArray = intArrayOf(1)
            GLES20.glGenFramebuffers(1, frameBufferArray, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0])
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                textureId,
                0
            )
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            if (DEBUG) Log.d(
                TAG, "createFrameBuffer frameBufferArray ${frameBufferArray[0]}")
            return frameBufferArray[0]
        }

        fun releaseFrameBuffer(frameBufferId: Int) {
            if (DEBUG) Log.d(
                TAG, "releaseFrameBuffer frameBufferId $frameBufferId")
            GLES20.glDeleteFramebuffers(1, IntArray(1) { frameBufferId }, 0)
        }
    }

    class BufferUtils {
        companion object {
            
            var SQUARE_VERTICES = floatArrayOf(
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f
            )

            var TEXTURE_VERTICES = floatArrayOf(
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
            )

            fun generateFloatBuffer(size: Int) =
                ByteBuffer.allocateDirect(4 * size)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer() as FloatBuffer

            fun generateFloatBuffer(arrayBuffer: FloatArray) =
                ByteBuffer.allocateDirect(4 * arrayBuffer.size)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(arrayBuffer)
                    .rewind() as FloatBuffer
        }
    }

    class TextureCoordinateUtils {
        companion object {
            fun generateBitmapTextureCoordinate(
                textureWidth: Int,
                textureHeight: Int,
                targetWidth: Int,
                targetHeight: Int
            ): FloatArray =
                generateTextureCoordinate(
                    textureWidth,
                    textureHeight,
                    targetWidth,
                    targetHeight,
                    true
                )

            fun generateTextureCoordinate(
                textureWidth: Int,
                textureHeight: Int,
                targetWidth: Int,
                targetHeight: Int,
                isBitmap: Boolean = false
            ): FloatArray {
                val containerRatio = targetWidth.toFloat() / targetHeight
                val ratio = textureWidth.toFloat() / textureHeight
                var t = 1f
                var b = 0f
                var l = 0f
                var r = 1f
                if (containerRatio > ratio) {
                    val requestTargetHeight = textureWidth / containerRatio
                    val offset = (textureHeight - requestTargetHeight) / 2
                    t = (requestTargetHeight + offset) / textureHeight
                    b = offset / textureHeight
                } else {
                    val requestTargetWidth = textureHeight * containerRatio
                    val offset = (textureWidth - requestTargetWidth) / 2
                    l = offset / textureWidth
                    r = (requestTargetWidth + offset) / textureWidth
                }
                if (isBitmap) {
                    return floatArrayOf(
                        l, b,
                        l, t,
                        r, b,
                        r, t
                    )
                }
                return floatArrayOf(
                    l, t,
                    l, b,
                    r, t,
                    r, b
                )
            }
        }
    }

    class DebugUtils {
        companion object {
            @Throws(IOException::class)
            fun saveFrame(
                context: Context,
                mWidth: Int,
                mHeight: Int,
                fileName: String
            ) {
                val mPixelBuf =
                    ByteBuffer.allocateDirect(mWidth * mHeight * 4)
                mPixelBuf.order(ByteOrder.LITTLE_ENDIAN)
                mPixelBuf.rewind()
                GLES20.glReadPixels(
                    0,
                    0,
                    mWidth,
                    mHeight,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    mPixelBuf
                )
                var bos: BufferedOutputStream? = null
                try {
                    val file = File(
                        context.getExternalCacheDir(),
                        fileName + "-" + System.currentTimeMillis() + ".png"
                    )
                    bos = BufferedOutputStream(FileOutputStream(file))
                    val bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
                    mPixelBuf.rewind()
                    bmp.copyPixelsFromBuffer(mPixelBuf)
                    bmp.compress(Bitmap.CompressFormat.PNG, 90, bos)
                    bmp.recycle()
                } finally {
                    bos?.close()
                }
            }
        }
    }
}