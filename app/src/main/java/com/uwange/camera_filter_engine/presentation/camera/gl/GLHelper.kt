package com.uwange.camera_filter_engine.presentation.camera.gl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.uwange.camera_filter_engine.presentation.camera.gl.shader.FilterShader

object GLHelper {
    fun createExternalTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        val textureId = textureIds[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        return textureId
    }

    fun buildProgram(shader: FilterShader): Int {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, shader.vertexShaderCode)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, shader.fragmentShaderCode)

        return GLES20.glCreateProgram().also { program ->
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
        }
    }

    private fun compileShader(type: Int, code: String): Int =
        GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)
        }
}