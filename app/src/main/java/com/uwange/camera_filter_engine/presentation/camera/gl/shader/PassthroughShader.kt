package com.uwange.camera_filter_engine.presentation.camera.gl.shader

class PassthroughShader: FilterShader {
    override val fragmentShaderCode: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        
        uniform samplerExternalOES uTexture;
        varying vec2 vTexCoord;
        
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()
}