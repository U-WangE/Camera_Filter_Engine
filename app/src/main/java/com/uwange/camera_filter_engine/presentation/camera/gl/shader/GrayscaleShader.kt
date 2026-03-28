package com.uwange.camera_filter_engine.presentation.camera.gl.shader

class GrayscaleShader: FilterShader {
    override val fragmentShaderCode: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;

        uniform samplerExternalOES uTexture;
        varying vec2 vTexCoord;

        void main() {
            vec4 color = texture2D(uTexture, vTexCoord);
            float gray = dot(color.rgb, vec3(0.3, 0.59, 0.11));
            gl_FragColor = vec4(gray, gray, gray, color.a);
        }
    """.trimIndent()
}