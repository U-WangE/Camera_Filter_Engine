package com.uwange.camera_filter_engine.presentation.camera.gl

import com.uwange.camera_filter_engine.domain.filter.FrameFilter

interface GlFrameFilter : FrameFilter {
    val vertexShaderCode: String
        get() = """
              attribute vec4 aPosition;
              attribute vec2 aTexCoord;
              varying vec2 vTexCoord;
              
              void main() {
                  gl_Position = aPosition;
                  vTexCoord = aTexCoord;
              }
          """.trimIndent()

    val fragmentShaderCode: String
}
