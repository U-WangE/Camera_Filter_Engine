package com.uwange.camera_filter_engine.presentation.camera

import com.uwange.camera_filter_engine.core.mvi.MviViewModel
import com.uwange.camera_filter_engine.domain.camera.model.CameraPermissionStatus
import com.uwange.camera_filter_engine.domain.camera.usecase.ResolveCameraPermissionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val resolveCameraPermissionStatusUseCase: ResolveCameraPermissionStatusUseCase,
) : MviViewModel<CameraState, CameraIntent, CameraEffect>(CameraState()) {

    override fun onIntent(intent: CameraIntent) {
        when (intent) {
            CameraIntent.RequestPermission -> sendEffect(CameraEffect.LaunchPermissionRequest)
            is CameraIntent.PermissionResult -> handlePermissionResult(intent)
            CameraIntent.DismissPermissionDialog -> setState { it.copy(isDialogVisible = false) }
            CameraIntent.OpenAppSettings -> sendEffect(CameraEffect.OpenAppSettings)
            is CameraIntent.SelectFilter -> setState { it.copy(filterType = intent.type) }
        }
    }

    private fun handlePermissionResult(intent: CameraIntent.PermissionResult) {
        val status = resolveCameraPermissionStatusUseCase(
            granted = intent.granted,
            shouldShowRationale = intent.shouldShowRationale,
        )

        setState {
            it.copy(
                permissionStatus = status,
                isDialogVisible = status != CameraPermissionStatus.Granted,
            )
        }
    }
}
