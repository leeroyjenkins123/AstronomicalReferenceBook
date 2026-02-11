package com.example.pmu2

import android.opengl.GLES20
import android.opengl.Matrix

class CubeCursor {
    private var cube: ColorCube = ColorCube()

    val objectPositions = mutableListOf<Float>()
    val objectRadii = mutableListOf<Float>()
    var selectedIndex = 0

    init {
        objectPositions.clear()
        objectPositions += 0f     // Sun
        objectPositions += 0.8f   // Mercury
        objectPositions += 1.2f   // Venus
        objectPositions += 1.6f   // Earth
        objectPositions += 1.6f // Moon
        objectPositions += 2.2f   // Mars
        objectPositions += 2.8f   // Jupiter
        objectPositions += 3.6f   // Saturn
        objectPositions += 4.0f   // Uranus
        objectPositions += 4.4f   // Neptune



        objectRadii.clear()
        objectRadii += 0.5f
        objectRadii += 0.15f
        objectRadii += 0.18f
        objectRadii += 0.2f
        objectRadii += 0.08f
        objectRadii += 0.2f
        objectRadii += 0.30f
        objectRadii += 0.26f
        objectRadii += 0.22f
        objectRadii += 0.20f

    }

    fun drawCursor(angle: Float, vpMatrix: FloatArray, scale: Float) {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)

        val radius = objectRadii[selectedIndex]
        val cubeScale = radius * 2f * 1.2f

        val orbit = objectPositions[selectedIndex]
        val speed = getPlanetSpeed(selectedIndex)

        Matrix.rotateM(model,0,angle*speed,0f,1f,0f)
        Matrix.translateM(model, 0, orbit * scale, 0f, 0f)

        if(selectedIndex == 4){
            Matrix.rotateM(model, 0, angle * (speed*2), 0f, 1f, 0f)
            Matrix.translateM(model, 0, 0.3f* scale, 0f, 0f)
        }

        Matrix.scaleM(model, 0, cubeScale* scale, cubeScale* scale, cubeScale* scale)
        Matrix.rotateM(model,0,angle,0f,1f,0f)
        Matrix.rotateM(model,0,20f,1f,0f,0f)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        cube.draw(mvp)

        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun getPlanetSpeed(index: Int): Float {
        return when (index) {
            0 -> 0f
            1 -> 4f    // Mercury
            2 -> 3f    // Venus
            3 -> 2f    // Earth
            4 -> 2f
            5 -> 1.6f  // Mars
            6 -> 1.2f  // Jupiter
            7 -> 1f    // Saturn
            8 -> 0.8f  // Uranus
            9 -> 0.6f  // Neptune
            else -> 0f
        }
    }
}