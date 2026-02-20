package com.example.pmu2

import android.content.Context
import android.content.Intent
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.pmu2.GLBackgroundSquare
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.rem

class OpenGLRenderer(private val context: Context, private val currentIndex: Int) : GLSurfaceView.Renderer {
    private lateinit var background: GLBackgroundSquare

    private lateinit var borderLines: GLLines

    lateinit var cubeCursor: CubeCursor
    // Планеты
    private lateinit var sun: Planet
    private lateinit var mercury: Planet
    private lateinit var venus: Planet
    private lateinit var earth: Planet
    private lateinit var mars: Planet
    private lateinit var jupiter: Planet
    private lateinit var saturn: Planet
    private lateinit var uranus: Planet
    private lateinit var neptune: Planet

    // Луна
    private lateinit var moonSphere: SpherePlanet

    private lateinit var blackHole: SpherePlanet

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)

    private var ratio = 0f
    private var angle = 0f


    // Позиция чёрной дыры
    private var blackHoleX = 0f
    private var blackHoleY = 0f
    private var blackHoleZ = -50f

    // Скорость движения
    private var blackHoleSpeedX = 0.08f
    private var blackHoleSpeedY = 0.05f

    // Флаг исчезновения
    private var blackHoleVisible = true

    private var bgLimitX = 0f
    private var bgLimitY = 0f

    private var holeLimitX = 0f
    private var holeLimitY = 0f



    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        background = GLBackgroundSquare(context)

        cubeCursor = CubeCursor()

        borderLines = GLLines()

        blackHole = SpherePlanet(context, R.drawable.black_hole)

        cubeCursor.selectedIndex = currentIndex

        // Планеты (сферы)
        sun = Planet(SpherePlanet(context,R.drawable.sun1), 0f, 0.5f, 0f)
        mercury = Planet(SpherePlanet(context,R.drawable.mercury1), 0.8f, 0.15f, 4f)
        venus = Planet(SpherePlanet(context,R.drawable.venus1), 1.2f, 0.18f, 3f)
        earth = Planet(SpherePlanet(context,R.drawable.earth1), 1.6f, 0.2f, 2f)
        mars = Planet(SpherePlanet(context,R.drawable.mars1), 2.2f, 0.2f, 1.6f)
        jupiter = Planet(SpherePlanet(context,R.drawable.jupiter1), 2.8f, 0.30f, 1.2f)
        saturn = Planet(SpherePlanet(context,R.drawable.saturn1), 3.6f, 0.26f, 1f)
        uranus = Planet(SpherePlanet(context,R.drawable.uranus1), 4.0f, 0.22f, 0.8f)
        neptune = Planet(SpherePlanet(context,R.drawable.neptun1), 4.4f, 0.20f, 0.6f)

        moonSphere = SpherePlanet(context,R.drawable.moon1)

        // Камера фиксированная
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 3f, 10f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        ratio = width.toFloat() / height

        if (ratio > 1f) {
            bgLimitX = 97f * ratio
            bgLimitY = 97f
        } else {
            bgLimitX = 53f
            bgLimitY = 53f / ratio
        }

        val backgroundZ = -80f
        val depthFactor = blackHoleZ / backgroundZ // оба отрицательные, получится положительное число

        holeLimitX = bgLimitX * depthFactor
        holeLimitY = bgLimitY * depthFactor


        Matrix.frustumM(
            projectionMatrix, 0,
            -ratio, ratio,
            -1f, 1f,
            1f, 100f
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        angle += 0.5f
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // масштаб всей системы от ориентации
        val systemScale = if (ratio > 1f) {
            2f   // альбомная — ближе
        } else {
            1f   // портретная — дальше
        }
        updateBlackHole()

        drawBackground()
        drawBlackHoleBorders()

        drawBlackHole(systemScale)
        drawSolarSystem(systemScale)
        cubeCursor.drawCursor(angle, vpMatrix, systemScale)
    }

    private fun drawBackground() {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)

        // немного наклоняем фон под камеру
        Matrix.rotateM(model, 0, -16.8f, 1f, 0f, 0f)
        Matrix.translateM(model, 0, 0f, 0f, -80f)

        if (ratio > 1f)
            Matrix.scaleM(model, 0, 91f * ratio, 91f, 1f)
        else
            Matrix.scaleM(model, 0, 50f, 50f / ratio, 1f)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)
        background.draw(mvp)
    }

    private fun drawSolarSystem(scale: Float) {
        sun.drawScaled(vpMatrix, angle, scale)
        mercury.drawScaled(vpMatrix, angle, scale)
        venus.drawScaled(vpMatrix, angle, scale)
        earth.drawScaled(vpMatrix, angle, scale)

        drawMoonScaled(scale)

        mars.drawScaled(vpMatrix, angle, scale)
        jupiter.drawScaled(vpMatrix, angle, scale)
        saturn.drawScaled(vpMatrix, angle, scale)
        uranus.drawScaled(vpMatrix, angle, scale)
        neptune.drawScaled(vpMatrix, angle, scale)
    }

    private fun drawMoonScaled(scale: Float) {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)

        // орбита Земли
        Matrix.rotateM(model, 0, angle * 2f, 0f, 1f, 0f)
        Matrix.translateM(model, 0, 1.6f * scale, 0f, 0f)

        // орбита Луны перпендикулярно эклиптике
        Matrix.rotateM(model, 0, angle * 4f, 0f, 1f, 0f)
        Matrix.translateM(model, 0, 0.3f * scale, 0f, 0f)

        Matrix.scaleM(model, 0, 0.08f * scale, 0.08f * scale, 0.08f * scale)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)
        moonSphere.draw(mvp)
    }

    private fun drawBlackHoleBorders() {
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)

        // рамка должна быть на том же Z, что и чёрная дыра
        Matrix.rotateM(model, 0, -16.8f, 1f, 0f, 0f)
        Matrix.translateM(model, 0, 0f, 0f, blackHoleZ)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)

        borderLines.drawRect(
            -holeLimitX,
            holeLimitX,
            -holeLimitY,
            holeLimitY,
            mvp
        )
    }

    private fun updateBlackHole() {
        // Движение
        blackHoleX += blackHoleSpeedX
        blackHoleY += blackHoleSpeedY

        // Отскок по X
        if (blackHoleX > holeLimitX) {
            blackHoleX = holeLimitX
            blackHoleSpeedX = -blackHoleSpeedX
        }
        if (blackHoleX < -holeLimitX) {
            blackHoleX = -holeLimitX
            blackHoleSpeedX = -blackHoleSpeedX
        }

        if (blackHoleY > holeLimitY) {
            blackHoleY = holeLimitY
            blackHoleSpeedY = -blackHoleSpeedY
        }
        if (blackHoleY < -holeLimitY) {
            blackHoleY = -holeLimitY
            blackHoleSpeedY = -blackHoleSpeedY
        }

    }

    private fun drawBlackHole(scale: Float) {
        if (!blackHoleVisible) return

        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)
        Matrix.rotateM(model, 0, -16.8f, 1f, 0f, 0f)
        // Позиция
        Matrix.translateM(model, 0, blackHoleX, blackHoleY, blackHoleZ)

        Matrix.rotateM(model, 0, 25f,0f,1f,0f)
        // Вращение
//        Matrix.rotateM(model, 0, angle * 2f, 0f, 0f, 1f)

        // Размер
        Matrix.scaleM(model, 0, 0.8f*scale, 0.8f*scale, 0.8f*scale)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, model, 0)

        blackHole.draw(mvp)
    }

    fun selectNext() {
        cubeCursor.selectedIndex = (cubeCursor.selectedIndex + 1) % cubeCursor.objectPositions.size
    }

    fun selectPrev() {
        cubeCursor.selectedIndex = if (cubeCursor.selectedIndex == 0) cubeCursor.objectPositions.size - 1 else cubeCursor.selectedIndex - 1
    }

    fun getSelectedIndex(): Int = cubeCursor.selectedIndex

    fun setSelectedIndex(index: Int) {
        cubeCursor.selectedIndex = index
    }


}