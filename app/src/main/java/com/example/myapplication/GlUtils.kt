package com.example.myapplication

import android.graphics.PointF
import android.opengl.GLES30

class GlUtils {
    companion object {
        const val COORDS_PER_VERTEX = 3
        const val RECT_VERTEX_COUNT = 4

        fun loadShader(type: Int, shaderCode: String): Int {
            return GLES30.glCreateShader(type).also { shader ->
                GLES30.glShaderSource(shader, shaderCode)
                GLES30.glCompileShader(shader)
            }
        }

        /**
         * Convert from screen space to OpenGL coordinate space
         */
        fun toGlCoords(scrWidth: Int, scrHeight: Int, x: Int, y: Int): PointF {
                return PointF((x * 2.0 / scrWidth - 1.0).toFloat(), (y * -2.0 / scrHeight + 1.0).toFloat())
        }
    }
}