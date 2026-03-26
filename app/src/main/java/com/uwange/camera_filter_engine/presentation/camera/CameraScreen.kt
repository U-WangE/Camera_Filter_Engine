package com.uwange.camera_filter_engine.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CameraRoute(
    viewModel: CameraViewModel = hiltViewModel()
) {
    CameraScreen()
}

@Composable
fun CameraScreen() {
}

@Preview
@Composable
private fun PreviewCameraScreen() {
}
