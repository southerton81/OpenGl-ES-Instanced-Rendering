package com.example.myapplication

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * Draws rectangle(s)
 */
class RectsGl(rcsCoords: FloatArray) {

    private val vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}"

    private val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}"

    private var program: Int
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var vao = IntBuffer.allocate(1)
    private val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
    private val rcsCount: Int = rcsCoords.size / GlUtils.COORDS_PER_VERTEX / GlUtils.RECT_VERTEX_COUNT
    private val vertexStride: Int = GlUtils.COORDS_PER_VERTEX * 4 // 4 bytes (Float) per vertex
    private val vertexBufferSize = rcsCoords.size * 4
    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexBufferSize).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(rcsCoords)
            position(0)
        }
    }

    init {
        val vertexShader = GlUtils.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = GlUtils.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }

        GLES30.glUseProgram(program)
    }

    fun allocateBuffer() {
        val vbo = IntBuffer.allocate(1)
        GLES30.glGenBuffers(1, vbo)
        GLES30.glGenVertexArrays(1, vao)
        GLES30.glBindVertexArray(vao[0])
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBufferSize, vertexBuffer, GLES30.GL_STATIC_DRAW)

        positionHandle = GLES30.glGetAttribLocation(program, "vPosition")
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(
                positionHandle,
                GlUtils.COORDS_PER_VERTEX,
                GLES30.GL_FLOAT,
                false,
                vertexStride,
                0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)

        colorHandle = GLES30.glGetUniformLocation(program, "vColor").also {
            GLES30.glUniform4fv(it, 1, color, 0)
        }
    }

    fun draw() {
        GLES30.glBindVertexArray(vao[0])
        var first = 0
        // Cycle that works same as glDrawArraysInstanced()
        for (i in 0 until rcsCount) {
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, first, GlUtils.RECT_VERTEX_COUNT)
            first += GlUtils.RECT_VERTEX_COUNT
        }
        GLES30.glBindVertexArray(0)
    }
}