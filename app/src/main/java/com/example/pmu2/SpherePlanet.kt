package com.example.pmu2

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SpherePlanet(
    private val context: Context,
    private val textureResId: Int,  // Например, R.drawable.earth
    stacks: Int = 24,
    slices: Int = 48
) {

    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val program: Int
    private val indexCount: Int
    private var textureId: Int = 0

    private val vertexShader = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        uniform mat4 uMVPMatrix;
        varying vec2 vTexCoord;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord;
        }
    """

    private val fragmentShader = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """

    init {
        val vertices = ArrayList<Float>()
        val texCoords = ArrayList<Float>()
        val indices = ArrayList<Short>()

        // Генерация вершин ПОЛНОЙ сферы
        for (i in 0..stacks) {
            val phi = PI * i / stacks
            val y = cos(phi).toFloat()
            val r = sin(phi).toFloat()

            // Текстурная координата V (от 0 до 1 сверху вниз)
            val v = i.toFloat() / stacks  // без инверсии — инвертируем в шейдере или оставим как есть

            for (j in 0..slices) {
                val theta = 2.0 * PI * j / slices
                val x = (r * cos(theta)).toFloat()
                val z = (r * sin(theta)).toFloat()

                // Текстурная координата U (от 0 до 1 по долготе)
                val u = j.toFloat() / slices

                vertices.add(x)
                vertices.add(y)
                vertices.add(z)

                texCoords.add(u)
                texCoords.add(v)  // или 1.0f - v, если текстура перевёрнута
            }
        }

        // Генерация индексов
        for (i in 0 until stacks) {
            val k1 = i * (slices + 1)
            val k2 = k1 + slices + 1

            for (j in 0 until slices) {
                indices.add((k1 + j).toShort())
                indices.add((k2 + j).toShort())
                indices.add((k1 + j + 1).toShort())

                indices.add((k1 + j + 1).toShort())
                indices.add((k2 + j).toShort())
                indices.add((k2 + j + 1).toShort())
            }
        }

        indexCount = indices.size

        // Буфер вершин
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices.toFloatArray())
                position(0)
            }

        // Буфер текстурных координат
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoords.toFloatArray())
                position(0)
            }

        // Буфер индексов
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices.toShortArray())
                position(0)
            }

        // Компиляция шейдеров
        val v = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val f = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, v)
            GLES20.glAttachShader(it, f)
            GLES20.glLinkProgram(it)
        }

        // Загрузка текстуры
        textureId = loadTexture(context, textureResId)
    }

    fun draw(mvp: FloatArray) {
        GLES20.glUseProgram(program)

        val pos = GLES20.glGetAttribLocation(program, "aPosition")
        val tex = GLES20.glGetAttribLocation(program, "aTexCoord")
        val mvpH = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val textureH = GLES20.glGetUniformLocation(program, "uTexture")

        // Включаем атрибуты
        GLES20.glEnableVertexAttribArray(pos)
        GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)

        GLES20.glEnableVertexAttribArray(tex)
        GLES20.glVertexAttribPointer(tex, 2, GLES20.GL_FLOAT, false, 2 * 4, texCoordBuffer)

        // MVP и текстура
        GLES20.glUniformMatrix4fv(mvpH, 1, false, mvp, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureH, 0)

        // Рисуем
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indexCount,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        // Отключаем
        GLES20.glDisableVertexAttribArray(pos)
        GLES20.glDisableVertexAttribArray(tex)
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun loadTexture(context: Context, resourceId: Int): Int {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] != 0) {
            val options = BitmapFactory.Options().apply {
                inScaled = false
            }

            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

            // Настройки текстуры
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }

        return textureHandle[0]
    }
}
