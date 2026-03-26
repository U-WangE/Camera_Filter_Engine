package com.uwange.camera_filter_engine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.uwange.camera_filter_engine.presentation.camera.CameraRoute
import com.uwange.camera_filter_engine.ui.theme.Camera_Filter_EngineTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Camera_Filter_EngineTheme {
                CameraRoute()
            }
        }
    }
}
