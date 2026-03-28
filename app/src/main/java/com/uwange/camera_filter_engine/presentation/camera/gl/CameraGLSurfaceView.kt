package com.uwange.camera_filter_engine.presentation.camera.gl

import android.content.Context
import android.opengl.GLSurfaceView

class CameraGLSurfaceView(context: Context): GLSurfaceView(context) {
    private val renderer = CameraRenderer()

    init {
        initGLSurfaceView()
    }

    private fun initGLSurfaceView() {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}