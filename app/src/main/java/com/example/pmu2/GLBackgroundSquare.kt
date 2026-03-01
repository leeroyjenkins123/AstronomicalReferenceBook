//package com.example.pmu2
//
//import android.content.Context
//import android.graphics.BitmapFactory
//import android.opengl.GLES20
//import android.opengl.GLUtils
//import androidx.compose.ui.graphics.Matrix
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.FloatBuffer
//
//class GLBackgroundSquare(private val context: Context) {
//
//    private val vertexCordsData: FloatBuffer
//    private val textureCordsData: FloatBuffer
//    private val program: Int
//    private val textureId: Int
//
//    // Вершинный шейдер для работы с каждой вершиной
//    private val vertexShaderCode = """
//        attribute vec4 aPosition; // переменная вектора из 4 переменных для хранения позиции вершины (x,y,z,w)
//        attribute vec2 aTexCoord; // вектор из 2 переменных для привязки к вершине точку из текстуры (u,v)
//        uniform mat4 uMVPMatrix; // одинаковая для всех вершин матрица положения объекта, положения камеры и перспективы для расположения на экране
//        varying vec2 vTexCoord; // вектор текстурных координат для определения цветов из текстуры для пикселей в текстурном шейдере
//        void main(){
//            gl_Position = uMVPMatrix * aPosition; // итоговая позиция в пространстве
//            vTexCoord = aTexCoord;
//        }
//    """.trimIndent()
//
//    // фрагментный шейдер
//    private val fragmentShaderCode = """
//        precision mediump float; // средняя точность для текстуры
//        varying vec2 vTexCoord;
//        uniform sampler2D uTexture; // одинаковое для каждого пикселя представление 2D-текстуры. Текстура, откуда брать цвета
//        void main(){
//            gl_FragColor = texture2D(uTexture, vTexCoord);
//        }
//    """.trimIndent()
//
//    init {
//        val vertices = floatArrayOf(
//            -1f,1f,0f, // левый верх
//            -1f,-1f,0f, // левый низ
//            1f,-1f,0f, // правый низ
//            1f,1f,0f // правый верх
//        )
//
//        val texCords = floatArrayOf(
//            0f,0f, // левый низ
//            0f,1f, //
//            1f,1f, //
//            1f,0f //
//        )
//
//        vertexCordsData = ByteBuffer.allocateDirect(vertices.size * 4)
//            .order(ByteOrder.nativeOrder())
//            .asFloatBuffer()
//            .apply {
//                put(vertices)
//                position(0)
//            }
//
//        textureCordsData = ByteBuffer.allocateDirect(texCords.size * 4)
//            .order(ByteOrder.nativeOrder())
//            .asFloatBuffer()
//            .apply {
//                put(texCords)
//                position(0)
//            }
//        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
//        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
//        program = GLES20.glCreateProgram().also {
//            GLES20.glAttachShader(it, vertexShader)
//            GLES20.glAttachShader(it, fragmentShader)
//            GLES20.glLinkProgram(it)
//        }
//
//        textureId = loadTexture(context, R.drawable.galaxy)
//    }
//
//    fun draw(mvpMatrix: FloatArray){
//        GLES20.glUseProgram(program)
//
//        val positionHandle = GLES20.glGetAttribLocation(program,"aPosition")
//        val texCordHandle = GLES20.glGetAttribLocation(program,"aTexCoord")
//        val mvpHandle = GLES20.glGetUniformLocation(program,"uMVPMatrix")
//        val textureHandle = GLES20.glGetUniformLocation(program,"uTexture")
//
//        GLES20.glEnableVertexAttribArray(positionHandle)
//        GLES20.glVertexAttribPointer(
//            positionHandle,
//            3, // сколько значений на все вершину
//            GLES20.GL_FLOAT,
//            false, // нормализация
//            3*4, // смещение между вершинами
//            vertexCordsData // откуда брать данные
//        )
//
//        GLES20.glEnableVertexAttribArray(texCordHandle)
//        GLES20.glVertexAttribPointer(
//            texCordHandle,
//            2,
//            GLES20.GL_FLOAT,
//            false,
//            2 * 4,
//            textureCordsData
//        )
//
//
//        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0) // загрузка матрицы в вершинный шейдер
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
//        GLES20.glUniform1i(textureHandle, 0)
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
//
//        GLES20.glDisableVertexAttribArray(positionHandle)
//        GLES20.glDisableVertexAttribArray(texCordHandle)
//    }
//}
//

//
//fun loadTexture(context: Context, resId: Int) : Int{
//    val textureIds = IntArray(1)
//    GLES20.glGenTextures(1,textureIds,0)
//
//    val options = BitmapFactory.Options().apply { inScaled = false }
//    val bitmap = BitmapFactory.decodeResource(context.resources,resId,options)
//
//    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureIds[0])
//    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
//    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
//
//    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0)
//    bitmap.recycle()
//
//    return textureIds[0]
//}

package com.example.pmu2

import android.content.Context
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLBackgroundSquare(private val context: Context) {

    private val vertexCordsData: FloatBuffer
    private val textureCordsData: FloatBuffer
    private val program: Int

    private val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        uniform mat4 uMVPMatrix;
        varying vec2 vTexCoord;
        void main(){
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision highp float;
        varying vec2 vTexCoord;
        
        uniform float uTime;
        uniform vec2 uResolution;
        uniform vec2 uBlackHolePos; // Координаты от 0.0 до 1.0

        #define iterations 12
        #define formuparam 0.57
        #define volsteps 10
        #define stepsize 0.2
        #define zoom 1.500
        #define tile 1.0
        #define speed 0.002
        #define brightness 0.0015
        #define darkmatter 1.00
        #define distfading 0.730
        #define saturation 1.0

        void main() {
            // Исправляем пропорции UV
            vec2 uv = vTexCoord - 0.5;
            uv.y *= uResolution.y / uResolution.x;
            
            // Позиция черной дыры (центрируем и правим аспект)
            vec2 bhPos = uBlackHolePos - 0.5;
            bhPos.y *= uResolution.y / uResolution.x;

            vec3 dir = vec3(uv * zoom, 1.0);
            float time = uTime * speed + 0.25;

            // --- ЭФФЕКТ ИСКАЖЕНИЯ (Гравитационная линза) ---
            vec2 diff = uv - bhPos;
            float distSq = dot(diff, diff);
            float bhRadiusSq = 0.0015; // Размер визуального горизонта событий
            float bhIntensity = 0.04; // Сила искажения

            if (distSq > bhRadiusSq) {
                // Искажаем вектор направления луча в зависимости от близости к ЧД
                float force = bhIntensity / (distSq + 0.01);
                dir.xy -= diff * force;

                // --- STAR NEST RENDER ---
                float s = 0.1, fade = 1.0;
                vec3 v = vec3(0.0);
                vec3 from = vec3(1.0, 0.5, 0.5); 
                from += vec3(time * 2.0, time, -2.0);

                for (int r = 0; r < volsteps; r++) {
                    vec3 p = from + s * dir * 0.5;
                    p = abs(vec3(tile) - mod(p, vec3(tile * 2.0)));
                    float pa, a = pa = 0.0;
                    for (int i = 0; i < iterations; i++) {
                        p = abs(p) / dot(p, p) - formuparam;
                        a += abs(length(p) - pa);
                        pa = length(p);
                    }
                    float dm = max(0.0, darkmatter - a * a * 0.001);
                    a *= a * a;
                    if (r > 6) fade *= 1.0 - dm;
                    v += fade;
                    v += vec3(s, s * s, s * s * s * s) * a * brightness * fade;
                    fade *= distfading;
                    s += stepsize;
                }
                v = mix(vec3(length(v)), v, saturation);
                gl_FragColor = vec4(v * 0.01, 1.0);
            } else {
                // Внутренняя часть черной дыры (тьма)
                gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
            }
        }
    """.trimIndent()

    init {
        val vertices = floatArrayOf(-1f, 1f, 0f, -1f, -1f, 0f, 1f, -1f, 0f, 1f, 1f, 0f)
        val texCords = floatArrayOf(0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f)

        vertexCordsData = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices); position(0) }
        textureCordsData = ByteBuffer.allocateDirect(texCords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(texCords); position(0) }

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray, time: Float, width: Float, height: Float, bhX: Float, bhY: Float) {
        GLES20.glUseProgram(program)

        val posHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val texHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val timeHandle = GLES20.glGetUniformLocation(program, "uTime")
        val resHandle = GLES20.glGetUniformLocation(program, "uResolution")
        val bhHandle = GLES20.glGetUniformLocation(program, "uBlackHolePos")

        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 12, vertexCordsData)
        GLES20.glEnableVertexAttribArray(texHandle)
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 8, textureCordsData)

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(timeHandle, time)
        GLES20.glUniform2f(resHandle, width, height)
        GLES20.glUniform2f(bhHandle, bhX, bhY) // Передаем нормализованные координаты

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(texHandle)
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        return shader
    }
}

fun loadShader(type: Int, code: String) : Int{
    return GLES20.glCreateShader(type).also { shader->
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
    }
}