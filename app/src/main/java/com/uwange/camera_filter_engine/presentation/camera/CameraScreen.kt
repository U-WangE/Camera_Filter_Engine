package com.uwange.camera_filter_engine.presentation.camera

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CameraRoute(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onIntent(
            CameraIntent.OnPermissionResult(
                granted = granted
            )
        )
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PERMISSION_GRANTED

        if (isGranted)
            viewModel.onIntent(CameraIntent.OnPermissionResult(granted = true))
        else
            viewModel.onIntent(CameraIntent.OnEnterScreen)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CameraEffect.RequestCameraPermission -> {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    when {
        state.hasCameraPermission -> {
            CameraScreen()
        }
        state.isPermissionChecked -> {
            // TODO:: 권한 거부 시 필요 이유 설명 팝업
        }
    }
}

@Composable
fun CameraScreen() {
}

@Preview
@Composable
private fun PreviewCameraScreen() {
}
