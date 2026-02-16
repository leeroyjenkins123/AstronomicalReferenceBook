package com.example.pmu2

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLInfoRenderer(
    private val context: Context,
    private val objectIndex: Int
) : GLSurfaceView.Renderer  {

    private lateinit var sphere: PhongTexturedSphere

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(9)

    private var angle = 0f
    private var ratio = 0f

    private var scale = 0f

    override fun onDrawFrame(gl: GL10?) {
        angle += 0.5f
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)
        Matrix.rotateM(modelMatrix, 0, angle * 0.5f, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, 25f, 1f, 0f, 0f)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0) // перевод из локальных координат в координаты камеры
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0) // перевод координат камеры в экранные пиксели

        // Расчет матрицы нормалей, так мы искажаем при масштабировании
        val temp = FloatArray(16)
        Matrix.invertM(temp, 0, mvMatrix, 0)
        Matrix.transposeM(temp, 0, temp, 0)
        normalMatrix[0] = temp[0]; normalMatrix[1] = temp[1]; normalMatrix[2] = temp[2]
        normalMatrix[3] = temp[4]; normalMatrix[4] = temp[5]; normalMatrix[5] = temp[6]
        normalMatrix[6] = temp[8]; normalMatrix[7] = temp[9]; normalMatrix[8] = temp[10]

        val lightPos = floatArrayOf(5f, 8f, 10f) // позиция всета в координатах камеры

        sphere.draw(
            mvpMatrix = mvpMatrix,
            mvMatrix = mvMatrix,
            normalMatrix = normalMatrix,
            lightPosEye = lightPos,
            viewPosEye = floatArrayOf(0f, 0f, 5f),
            ambientStrength = 0.18f,
            specularStrength = 0.7f,
            shininess = 32f
        )

    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int
    ) {
        GLES20.glViewport(0, 0, width, height)
        ratio = width.toFloat() / height

        scale = if (ratio > 1f) {
            3.5f
        } else {
            2f
        }

        Matrix.frustumM(
            projectionMatrix, 0,
            -ratio, ratio,
            -1f, 1f,
            1f, 100f
        )
    }

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        val textureId = when (objectIndex) {
            0 -> R.drawable.sun1
            1 -> R.drawable.mercury1
            2 -> R.drawable.venus1
            3 -> R.drawable.earth1
            4 ->R.drawable.moon1
            5 -> R.drawable.mars1
            6 -> R.drawable.jupiter1
            7 -> R.drawable.saturn1
            8 -> R.drawable.uranus1
            9 -> R.drawable.water
            else -> R.drawable.moon1
        }

        sphere = PhongTexturedSphere(context, textureId)

        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 5f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
    }
}