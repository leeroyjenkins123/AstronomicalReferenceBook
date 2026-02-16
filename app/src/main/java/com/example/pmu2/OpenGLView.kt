package com.example.pmu2

import android.content.Context
import android.opengl.GLSurfaceView

class OpenGLView(context: Context, index: Int): GLSurfaceView(context) {
    private val renderer: OpenGLRenderer

    init {
        setEGLContextClientVersion(2)

        renderer = OpenGLRenderer(context, index)
        setRenderer(renderer)

        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    fun getRenderer(): OpenGLRenderer = renderer
}