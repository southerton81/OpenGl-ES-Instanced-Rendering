package com.example.myapplication

import android.content.Context
import android.opengl.GLES30
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.View
import javax.microedition.khronos.opengles.GL10


const val FAST_DRAW_FLAG = true

class DrawRectsActivity : AppCompatActivity() {

    private var gLView: GLSurfaceView? = null

    class MyGLRenderer : GLSurfaceView.Renderer {

        var startTime = System.nanoTime()
        var frames = 0
        var parentWidth : Int = 0
        var parentHeight : Int = 0
        var rectsCoordsArray = floatArrayOf()
        var individualRects  = mutableListOf<RectsGl>()
        var instancedRects : RectsGl? = null
        val rectsCount = 5000

        override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

            val rectsArray = FloatArray(rectsCount
                    * 4 /* coordinates per rect*/
                    * 3 /* x, y, z components per coordinate*/)
            var index = 0

            // Generate rectangles of different sizes
            for (i in 0 until rectsCount) {
                val x : Int = ((Math.random() * parentHeight).toInt())
                val y : Int = ((Math.random() * parentWidth).toInt())
                val w : Int = (((Math.random() + .2) * 300).toInt())
                val h : Int = (((Math.random() + .2) * 300).toInt())

                val topLeft = GlUtils.toGlCoords(parentWidth, parentHeight, x,  y)
                val bottomLeft = GlUtils.toGlCoords(parentWidth, parentHeight, x, y + h)
                val bottomRight = GlUtils.toGlCoords(parentWidth, parentHeight, x + w, y + h)
                val topRight = GlUtils.toGlCoords(parentWidth, parentHeight,  x + w, y)

                rectsArray[index++] = topLeft.x; rectsArray[index++] = topLeft.y; rectsArray[index++] = 0.0f
                rectsArray[index++] = bottomLeft.x; rectsArray[index++] = bottomLeft.y; rectsArray[index++] = 0.0f
                rectsArray[index++] = bottomRight.x; rectsArray[index++] = bottomRight.y; rectsArray[index++] = 0.0f
                rectsArray[index++] = topRight.x; rectsArray[index++] = topRight.y; rectsArray[index++] = 0.0f
            }

            if (FAST_DRAW_FLAG) {
                instancedRects = RectsGl(rectsArray)
                instancedRects?.allocateBuffer()
            } else {
                var startIndex = 0
                for (i in 0 until rectsCount) {
                    val endIndex = startIndex + 4 * 3 - 1
                    val rectGl = RectsGl(rectsArray.sliceArray(IntRange(startIndex, endIndex)))
                    rectGl.allocateBuffer()
                    individualRects.add(rectGl)
                    startIndex = endIndex + 1
                }
            }
        }

        override fun onDrawFrame(unused: GL10) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

            if (FAST_DRAW_FLAG) {
                instancedRects?.draw()
            } else {
                individualRects.forEach {
                    it.draw()
                }
            }

            logFrame()
        }

        override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
        }

        private fun logFrame() {
            frames++
            if (System.nanoTime() - startTime >= 1000000000) {
                Log.i("FPSCounter", "fps: $frames")
                frames = 0
                startTime = System.nanoTime()
            }
        }
    }

    class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
        private val renderer: MyGLRenderer

        init {
            setEGLContextClientVersion(2)
            renderer = MyGLRenderer()
            setRenderer(renderer)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val parentWidth = View.MeasureSpec.getSize(widthMeasureSpec)
            val parentHeight = View.MeasureSpec.getSize(heightMeasureSpec)
            this.setMeasuredDimension(parentWidth, parentHeight)
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            renderer.parentHeight = parentHeight
            renderer.parentWidth = parentWidth
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gLView = MyGLSurfaceView(this)
        setContentView(gLView)
    }
}
