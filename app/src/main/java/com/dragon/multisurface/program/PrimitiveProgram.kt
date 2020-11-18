package com.dragon.multisurface.program

import android.opengl.GLES20
import com.dragon.multisurface.OpenGlUtils
import java.nio.FloatBuffer

class PrimitiveProgram(
    var type: Int = GLES20.GL_POINTS,
    var color: FloatBuffer = OpenGlUtils.BufferUtils.generateFloatBuffer(3).put(
        floatArrayOf(0.5f, 0.5f, 0.5f)
    ),
    var size: Int = 10
) : BasicProgram(
    """
            attribute vec2 vPosition;
            uniform mat4 mvpMatrix;
            uniform float size;
            void main(){
                gl_Position =  mvpMatrix * vec4(vPosition,0.0,1.0);
                gl_PointSize = size * size;
            }
        """, """
            precision mediump float;
            uniform vec3 color;
             void main(){
                gl_FragColor = vec4(color,1.0);
            }
        """
) {

    private val vPositionHandle by lazy { GLES20.glGetAttribLocation(programHandle, "vPosition") }
    private val mvpMatrixHandle by lazy { GLES20.glGetUniformLocation(programHandle, "mvpMatrix") }
    private val sizeHandle by lazy { GLES20.glGetUniformLocation(programHandle, "size") }
    private val colorHandle by lazy { GLES20.glGetUniformLocation(programHandle, "color") }

    fun draw(
        positions: FloatBuffer,
        mvpMatrix: FloatArray,
        type: Int,
        color: FloatBuffer,
        size: Int
    ) {
        GLES20.glUseProgram(programHandle)
        GLES20.glLineWidth(size.toFloat())
        GLES20.glEnableVertexAttribArray(vPositionHandle)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform3fv(colorHandle, 1, color)
        GLES20.glUniform1f(sizeHandle, size.toFloat())
        GLES20.glVertexAttribPointer(vPositionHandle, 2, GLES20.GL_FLOAT, false, 0, positions)
        GLES20.glDrawArrays(type, 0, positions.capacity() / 2)
        GLES20.glDisableVertexAttribArray(vPositionHandle)
        GLES20.glUseProgram(0)
    }

    override fun draw(
        textureId: Int,
        position: FloatBuffer,
        textureCoordinate: FloatBuffer,
        mvp: FloatArray
    ) {
        draw(position, mvp, type, color, size)
    }
}