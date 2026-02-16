package com.example.pmu2

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY

class OpenGLInfoView (
    context: Context,
    objectIndex: Int
) : GLSurfaceView(context) {

    val renderer: OpenGLInfoRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = OpenGLInfoRenderer(context, objectIndex)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}