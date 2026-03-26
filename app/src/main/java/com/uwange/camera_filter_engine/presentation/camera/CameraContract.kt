package com.uwange.camera_filter_engine.presentation.camera

data class CameraState(
    val hasCameraPermission: Boolean = false,
    val isPermissionChecked: Boolean = false
)

sealed interface CameraIntent {
    data object OnEnterScreen: CameraIntent
    data class OnPermissionResult(val granted: Boolean): CameraIntent
}

sealed interface CameraEffect {
    data object RequestCameraPermission : CameraEffect
}