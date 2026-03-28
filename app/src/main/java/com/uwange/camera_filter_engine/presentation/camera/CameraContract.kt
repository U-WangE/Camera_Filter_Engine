package com.uwange.camera_filter_engine.presentation.camera

import com.uwange.camera_filter_engine.core.mvi.UiEffect
import com.uwange.camera_filter_engine.core.mvi.UiIntent
import com.uwange.camera_filter_engine.core.mvi.UiState
import com.uwange.camera_filter_engine.domain.camera.model.CameraPermissionStatus
import com.uwange.camera_filter_engine.domain.camera.model.FilterType

data class CameraState(
    val permissionStatus: CameraPermissionStatus = CameraPermissionStatus.Idle,
    val filterType: FilterType = FilterType.NONE,
) : UiState

sealed interface CameraIntent : UiIntent {
    data object RequestPermission : CameraIntent
    data class PermissionResult(
        val granted: Boolean,
        val shouldShowRationale: Boolean,
    ) : CameraIntent

    data object OpenAppSettings : CameraIntent
    data class SelectFilter(val type: FilterType) : CameraIntent
}

sealed interface CameraEffect : UiEffect {
    data object LaunchPermissionRequest : CameraEffect
    data object OpenAppSettings : CameraEffect
}
