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

class PhongTexturedSphere(context: Context,
                          textureResId: Int,
                          stacks: Int = 48,
                          slices: Int = 48
)  {
    private val vertexBuffer: FloatBuffer
    private val normalBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer

    private val program: Int
    private val indexCount: Int

    private var textureId: Int = 0

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix; // матрица позиции
        uniform mat4 uMVMatrix; // матрица для расчета vPosition
        uniform mat3 uNormalMatrix; // нормаль
        
        attribute vec4 aPosition; // позиция вершины
        attribute vec3 aNormal; // нормаль
        attribute vec2 aTexCoord; // текстурные координаты
        
        varying vec3 vPosition;
        varying vec3 vNormal;
        varying vec2 vTexCoord;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition; // Преобразуем вершину в экранные координаты.
            vPosition = vec3(uMVMatrix * aPosition); // Позиция вершины в eye space.
            vNormal = normalize(uNormalMatrix * aNormal); // Правильная нормаль в eye space.
            vTexCoord = aTexCoord; // Передаём текстурные координаты.
        }
    """.trimIndent()

        /*
precision — задаёт точность вычислений с плавающей точкой
mediump — средняя точность (самая популярная на Android)
highp — высокая (медленнее, больше энергопотребление)
lowp — низкая (быстрее, но хуже качество)
float — все переменные типа float, vec3, vec4 и т.д. будут по умолчанию mediump
uniform — значение одинаковое для всех пикселей. Задаётся один раз перед glDrawElements.
sampler2D — специальный тип для текстуры (как "указатель" на картинку)
vec3 — вектор из 3 float (x,y,z или r,g,b)
float — обычное число с плавающей запятой
varying — значение интерполируется между вершинами для каждого пикселя.Эти переменные приходят из vertex shader
        */
    private val fragmentShaderCode = """
        precision mediump float; // Точность вычислений.
        // Параметры освещения.
        uniform sampler2D uTexture;                    // 2D-текстура (картинка планеты)
        uniform vec3 uLightPos;       // позиция источника света в eye-space
        uniform vec3 uViewPos;        // позиция камеры в eye-space (обычно 0,0,0)
        uniform float uAmbientStrength;   // сила окружающего света (0.0 – 1.0)
        uniform float uSpecularStrength;  // сила блика (0.3 – 1.0)
        uniform float uShininess;         // "блеск" поверхности (8 – 256+)

        varying vec3 vPosition;   // позиция текущего пикселя в eye-space
        varying vec3 vNormal;     // нормаль в eye-space (уже нормализованная)
        varying vec2 vTexCoord;   // текстурные координаты (UV)

        void main() {
            vec3 texColor = texture2D(uTexture, vTexCoord).rgb; // Берём цвет из текстуры.

            // Ambient — окружающее освещение (есть везде)
            vec3 ambient = uAmbientStrength * texColor;

            // Diffuse — рассеянный свет (Lambertian)
            vec3 norm = normalize(vNormal); // делает длину вектора = 1.0
            vec3 lightDir = normalize(uLightPos - vPosition);
            float diff = max(dot(norm, lightDir), 0.0); // скалярное произведение → косинус угла между нормалью и светом. если свет падает сзади (косинус < 0) → 0 (нет освещения)
            vec3 diffuse = diff * texColor; // 

            // Specular блик по Blinn-Phong (
            vec3 viewDir = normalize(uViewPos - vPosition);
            vec3 reflectDir = reflect(-lightDir, norm);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), uShininess);
            vec3 specular = uSpecularStrength * spec * vec3(1.0);

            vec3 result = ambient + diffuse + specular;
            gl_FragColor = vec4(result, 1.0);
        }
//                void main() {
//                    vec3 texColor = texture2D(uTexture, vTexCoord).rgb;
//                    vec3 n_normal=normalize(vNormal);
//                    vec3 lightvector = normalize(uLightPos - vPosition);
//                    vec3 lookvector = normalize(uViewPos - vPosition);
//                    float ambient=0.2;
//                    float k_diffuse=0.8;
//                    float k_specular=0.4;
//                    float diffuse = k_diffuse * max(dot(n_normal, lightvector), 0.0);
//                    vec3 reflectvector = reflect(-lightvector, n_normal);
//                    float specular = k_specular * pow( max(dot(lookvector,reflectvector),0.0), 40.0 );
//                    vec4 one=vec4(1.0,1.0,1.0,1.0);
//                    vec3 lighting = ambient * texColor + diffuse * texColor + specular * vec3(1.0);
//                    gl_FragColor = vec4(lighting, 1.0);
//                }
    """.trimIndent()

    init {
        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        // Генерация сферы
        for (i in 0..stacks) {
            val phi = PI * i / stacks
            val cosPhi = cos(phi).toFloat()
            val sinPhi = sin(phi).toFloat()

            for (j in 0..slices) {
                val theta = 2 * PI * j / slices
                val cosTheta = cos(theta).toFloat()
                val sinTheta = sin(theta).toFloat()

                val x = cosTheta * sinPhi
                val y = cosPhi
                val z = sinTheta * sinPhi

                vertices.add(x)
                vertices.add(y)
                vertices.add(z)

                // Нормаль = позиция (для unit-сферы)
                normals.add(x)
                normals.add(y)
                normals.add(z)

                // Текстурные координаты
                val u = j.toFloat() / slices
                val v = i.toFloat() / stacks
                texCoords.add(u)
                texCoords.add(v)
            }
        }

        // Индексы
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

        vertexBuffer = createFloatBuffer(vertices.toFloatArray())
        normalBuffer = createFloatBuffer(normals.toFloatArray())
        texCoordBuffer = createFloatBuffer(texCoords.toFloatArray())
        indexBuffer = createShortBuffer(indices.toShortArray())

        // Компиляция шейдеров
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        // Текстура
        loadTexture(context, textureResId)
    }

    private fun loadTexture(context: Context, resId: Int) {
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
    }

    fun draw(
        mvpMatrix: FloatArray,
        mvMatrix: FloatArray,
        normalMatrix: FloatArray,
        lightPosEye: FloatArray,
        viewPosEye: FloatArray,
        ambientStrength: Float = 0.2f,
        specularStrength: Float = 0.6f,
        shininess: Float = 32f
    ) {
        GLES20.glUseProgram(program)

        val posHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val normalHandle = GLES20.glGetAttribLocation(program, "aNormal")
        val texHandle = GLES20.glGetAttribLocation(program, "aTexCoord")

        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val mvHandle = GLES20.glGetUniformLocation(program, "uMVMatrix")
        val normalMatHandle = GLES20.glGetUniformLocation(program, "uNormalMatrix")
        val lightPosHandle = GLES20.glGetUniformLocation(program, "uLightPos")
        val viewPosHandle = GLES20.glGetUniformLocation(program, "uViewPos")
        val ambientHandle = GLES20.glGetUniformLocation(program, "uAmbientStrength")
        val specularHandle = GLES20.glGetUniformLocation(program, "uSpecularStrength")
        val shininessHandle = GLES20.glGetUniformLocation(program, "uShininess")
        val texSampler = GLES20.glGetUniformLocation(program, "uTexture")

        // Атрибуты
        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 12, normalBuffer)

        GLES20.glEnableVertexAttribArray(texHandle)
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)

        // Uniforms
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(mvHandle, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix3fv(normalMatHandle, 1, false, normalMatrix, 0)

        GLES20.glUniform3fv(lightPosHandle, 1, lightPosEye, 0)
        GLES20.glUniform3fv(viewPosHandle, 1, viewPosEye, 0)

        GLES20.glUniform1f(ambientHandle, ambientStrength)
        GLES20.glUniform1f(specularHandle, specularStrength)
        GLES20.glUniform1f(shininessHandle, shininess)

        // Текстура
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(texSampler, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
        GLES20.glDisableVertexAttribArray(texHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private fun createFloatBuffer(array: FloatArray): FloatBuffer =
        ByteBuffer.allocateDirect(array.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(array); position(0) }

    private fun createShortBuffer(array: ShortArray): ShortBuffer =
        ByteBuffer.allocateDirect(array.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(array); position(0) }
}