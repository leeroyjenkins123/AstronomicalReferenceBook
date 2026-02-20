package com.example.pmu2

import android.opengl.GLES20
import android.opengl.Matrix

class GLLines {
    private val mvpMatrix = FloatArray(16)

    private val program: Int

    init {
        val vertexShaderCode =
            "attribute vec2 aPos;" +
                    "uniform mat4 uMVP;" +
                    "void main() {" +
                    "  gl_Position = uMVP * vec4(aPos, 0.0, 1.0);" +
                    "}"

        val fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 uColor;" +
                    "void main() {" +
                    "  gl_FragColor = uColor;" +
                    "}"

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun drawRect(minX: Float, maxX: Float, minY: Float, maxY: Float, vpMatrix: FloatArray) {
        val vertices = floatArrayOf(
            minX, minY,
            maxX, minY,
            maxX, maxY,
            minX, maxY
        )

        GLES20.glUseProgram(program)

        val posHandle = GLES20.glGetAttribLocation(program, "aPos")
        val colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVP")

        GLES20.glUniform4f(colorHandle, 1f, 0f, 0f, 1f) // красная рамка

        GLES20.glEnableVertexAttribArray(posHandle)

        val bb = java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        bb.put(vertices)
        bb.position(0)

        GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 0, bb)

        Matrix.setIdentityM(mvpMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4)

        GLES20.glDisableVertexAttribArray(posHandle)
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)
        }
    }
}
