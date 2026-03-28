package com.uwange.camera_filter_engine.presentation.camera.gl

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.uwange.camera_filter_engine.domain.camera.model.FilterType
import com.uwange.camera_filter_engine.presentation.camera.gl.shader.FilterShader
import com.uwange.camera_filter_engine.presentation.camera.gl.shader.GrayscaleShader
import com.uwange.camera_filter_engine.presentation.camera.gl.shader.PassthroughShader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraRenderer: GLSurfaceView.Renderer {
    private val _surfaceTexture = MutableStateFlow<SurfaceTexture?>(null)
    val surfaceTexture: StateFlow<SurfaceTexture?> = _surfaceTexture.asStateFlow()
    private var programId: Int = 0
    private var textureId: Int = 0
    @Volatile private var isFrameAvailable: Boolean = false
    private val texMatrix = FloatArray(16)

    @Volatile private var pendingShader: FilterShader? = PassthroughShader()

    fun setFilter(type: FilterType) {
        pendingShader = when (type) {
            FilterType.NONE -> PassthroughShader()
            FilterType.GRAYSCALE -> GrayscaleShader()
        }
    }

    private fun applyPendingShader() {
        val shader = pendingShader ?: return
        if (programId != 0) GLES20.glDeleteProgram(programId)
        programId = GLHelper.buildProgram(shader)
        pendingShader = null
    }

    // frame마다 그림
    override fun onDrawFrame(gl: GL10?) {
        applyPendingShader()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (isFrameAvailable) {
            _surfaceTexture.value?.updateTexImage()
            _surfaceTexture.value?.getTransformMatrix(texMatrix)
            isFrameAvailable = false
        }

        GLES20.glUseProgram(programId)

        val uTexMatrix = GLES20.glGetUniformLocation(programId, "uTexMatrix")
        GLES20.glUniformMatrix4fv(uTexMatrix, 1, false, texMatrix, 0)

        val uTexture = GLES20.glGetUniformLocation(programId, "uTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(uTexture, 0)

        drawQuad(programId)
    }

    // viewport setting
    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int
    ) {
        GLES20.glViewport(0, 0, width, height)
    }

    // 초기화 (shader, texture)
    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        textureId = GLHelper.createExternalTexture()
        _surfaceTexture.value = SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener {
                isFrameAvailable = true
            }
        }

        applyPendingShader()
    }

    private fun drawQuad(programId: Int) {
        val vertices = floatArrayOf(
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            -1f,  1f, 0f, 1f,
            1f,  1f, 1f, 1f,
        )

        val buffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        buffer.position(0)

        val stride = 4 * 4

        val aPosition = GLES20.glGetAttribLocation(programId, "aPosition")
        val aTexCoord = GLES20.glGetAttribLocation(programId, "aTexCoord")

        buffer.position(0)
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, stride, buffer)
        GLES20.glEnableVertexAttribArray(aPosition)

        buffer.position(2)
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, stride, buffer)
        GLES20.glEnableVertexAttribArray(aTexCoord)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}