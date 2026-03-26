package com.uwange.camera_filter_engine.domain.camera.model

sealed interface CameraPermissionStatus {
    data object Idle : CameraPermissionStatus
    data object Granted : CameraPermissionStatus
    data object Denied : CameraPermissionStatus
    data object PermanentlyDenied : CameraPermissionStatus
}
