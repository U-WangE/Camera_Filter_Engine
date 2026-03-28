package com.uwange.camera_filter_engine.presentation.camera.gl

import android.opengl.GLES11Ext
import android.opengl.GLES20

class GlFilterChain {
    private val cameraVertexShaderCode = """
          attribute vec4 aPosition;
          attribute vec2 aTexCoord;
          varying vec2 vTexCoord;
          uniform mat4 uTexMatrix;
          void main() {
              gl_Position = aPosition;
              vTexCoord = (uTexMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;
          }
      """.trimIndent()

    private val cameraFragmentShaderCode = """
          #extension GL_OES_EGL_image_external : require
          precision mediump float;
          uniform samplerExternalOES uTexture;
          varying vec2 vTexCoord;
          void main() {
              gl_FragColor = texture2D(uTexture, vTexCoord);
          }
      """.trimIndent()

    private var cameraProgramId = 0
    private val cameraFbo = GlFrameBufferObject()
    private val filterFbos = mutableListOf<GlFrameBufferObject>()
    private val filterProgramIds = mutableListOf<Int>()

    private var viewportWidth = 0
    private var viewportHeight = 0

    @Volatile var pendingFilters: List<GlFrameFilter>? = null

    fun setup() {
        cameraProgramId = GlHelper.buildProgram(
            cameraVertexShaderCode,
            cameraFragmentShaderCode
        )
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        cameraFbo.setup(width, height)

        pendingFilters = pendingFilters ?: emptyList()
    }

    fun draw(cameraTextureId: Int, texMatrix: FloatArray) {
        pendingFilters?.let { filters ->
            rebuildFilterChain(filters)
            pendingFilters = null
        }

        if (filterProgramIds.isEmpty()) {
            GLES20.glViewport(0, 0, viewportWidth, viewportHeight)
            drawCameraOes(cameraTextureId, cameraProgramId, texMatrix)
            return
        }

        cameraFbo.bind()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawCameraOes(cameraTextureId, cameraProgramId, texMatrix)
        cameraFbo.unbind()

        var inputTextureId = cameraFbo.textureId
        for (i in filterProgramIds.indices) {
            if (i < filterFbos.size) {
                filterFbos[i].bind()
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                drawFbo(inputTextureId, filterProgramIds[i])
                filterFbos[i].unbind()
                inputTextureId = filterFbos[i].textureId
            } else {
                GLES20.glViewport(0, 0, viewportWidth, viewportHeight)
                drawFbo(inputTextureId, filterProgramIds[i])
            }
        }
    }

    private fun rebuildFilterChain(filters: List<GlFrameFilter>) {
        filterProgramIds.forEach { GLES20.glDeleteProgram(it) }
        filterProgramIds.clear()
        filterFbos.forEach { it.release() }
        filterFbos.clear()

        if (filters.isEmpty()) return

        filters.forEach { filter ->
            filterProgramIds.add(
                GlHelper.buildProgram(filter.vertexShaderCode, filter.fragmentShaderCode)
            )
        }

        repeat(filters.size - 1) {
            filterFbos.add(GlFrameBufferObject().apply { setup(viewportWidth, viewportHeight)
            })
        }
    }

    private fun drawCameraOes(textureId: Int, programId: Int, texMatrix: FloatArray) {
        GLES20.glUseProgram(programId)
        val uTexMatrix = GLES20.glGetUniformLocation(programId, "uTexMatrix")
        GLES20.glUniformMatrix4fv(uTexMatrix, 1, false, texMatrix, 0)
        val uTexture = GLES20.glGetUniformLocation(programId, "uTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(uTexture, 0)
        GlHelper.drawQuad(programId)
    }

    private fun drawFbo(textureId: Int, programId: Int) {
        GLES20.glUseProgram(programId)
        val uTexture = GLES20.glGetUniformLocation(programId, "uTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTexture, 0)
        GlHelper.drawQuad(programId)
    }

    fun release() {
        if (cameraProgramId != 0) {
            GLES20.glDeleteProgram(cameraProgramId)
            cameraProgramId = 0
        }
        cameraFbo.release()
        filterProgramIds.forEach { GLES20.glDeleteProgram(it) }
        filterProgramIds.clear()
        filterFbos.forEach { it.release() }
        filterFbos.clear()
    }
}