package com.uwange.camera_filter_engine.presentation.camera.gl.shader

interface FilterShader {
    val vertexShaderCode: String
        get() = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            uniform mat4 uTexMatrix;
            
            void main() {
                gl_Position = aPosition;
                vTexCoord = (uTexMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;
            }
        """.trimIndent()

    val fragmentShaderCode: String
}