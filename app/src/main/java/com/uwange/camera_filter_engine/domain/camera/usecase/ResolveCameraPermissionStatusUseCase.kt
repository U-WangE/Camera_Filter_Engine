package com.uwange.camera_filter_engine.domain.camera.usecase

import com.uwange.camera_filter_engine.domain.camera.model.CameraPermissionStatus
import com.uwange.camera_filter_engine.domain.camera.repository.CameraPermissionRepository
import javax.inject.Inject

class ResolveCameraPermissionStatusUseCase @Inject constructor(
    private val cameraPermissionRepository: CameraPermissionRepository,
) {
    operator fun invoke(
        granted: Boolean,
        shouldShowRationale: Boolean,
    ): CameraPermissionStatus {
        return cameraPermissionRepository.resolvePermissionStatus(
            granted = granted,
            shouldShowRationale = shouldShowRationale,
        )
    }
}
