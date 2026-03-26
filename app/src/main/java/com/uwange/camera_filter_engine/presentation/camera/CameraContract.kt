package com.uwange.camera_filter_engine.presentation.camera

import com.uwange.camera_filter_engine.core.mvi.UiEffect
import com.uwange.camera_filter_engine.core.mvi.UiIntent
import com.uwange.camera_filter_engine.core.mvi.UiState
import com.uwange.camera_filter_engine.domain.camera.model.CameraPermissionStatus

data class CameraState(
    val permissionStatus: CameraPermissionStatus = CameraPermissionStatus.Idle,
    val isDialogVisible: Boolean = false,
) : UiState

sealed interface CameraIntent : UiIntent {
    data object RequestPermission : CameraIntent
    data class PermissionResult(
        val granted: Boolean,
        val shouldShowRationale: Boolean,
    ) : CameraIntent

    data object DismissPermissionDialog : CameraIntent
    data object OpenAppSettings : CameraIntent
}

sealed interface CameraEffect : UiEffect {
    data object LaunchPermissionRequest : CameraEffect
    data object OpenAppSettings : CameraEffect
}
