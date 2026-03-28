package com.uwange.camera_filter_engine.presentation.camera.gl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.uwange.camera_filter_engine.domain.camera.model.FilterType
import com.uwange.camera_filter_engine.presentation.camera.gl.shader.GrayscaleShader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraRenderer : GLSurfaceView.Renderer {

    private val _surfaceTexture = MutableStateFlow<SurfaceTexture?>(null)
    val surfaceTexture: StateFlow<SurfaceTexture?> = _surfaceTexture.asStateFlow()

    private var cameraTextureId = 0
    @Volatile private var isFrameAvailable = false
    private val texMatrix = FloatArray(16)

    private val chain = GlFilterChain()

    // main 스레드에서 호출
    fun setFilter(type: FilterType) {
        chain.pendingFilters = when (type) {
            FilterType.NONE -> emptyList()
            FilterType.GRAYSCALE -> listOf(GrayscaleShader())
        }
    }

    // GL 스레드: 최초 1회 초기화
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        cameraTextureId = GlHelper.createExternalTexture()
        _surfaceTexture.value = SurfaceTexture(cameraTextureId).apply {
            setOnFrameAvailableListener { isFrameAvailable = true }
        }

        chain.setup()
    }

    // GL 스레드: 화면 크기 변경 시
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        chain.onSurfaceChanged(width, height)
    }

    // GL 스레드: 매 프레임
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (isFrameAvailable) {
            _surfaceTexture.value?.updateTexImage()
            _surfaceTexture.value?.getTransformMatrix(texMatrix)
            isFrameAvailable = false
        }

        chain.draw(
            cameraTextureId = cameraTextureId,
            texMatrix = texMatrix,
        )
    }
}