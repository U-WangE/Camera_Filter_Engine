package com.uwange.camera_filter_engine.data.camera

import com.uwange.camera_filter_engine.domain.camera.model.CameraPermissionStatus
import com.uwange.camera_filter_engine.domain.camera.repository.CameraPermissionRepository
import javax.inject.Inject

class CameraPermissionRepositoryImpl @Inject constructor() : CameraPermissionRepository {
    override fun resolvePermissionStatus(
        granted: Boolean,
        shouldShowRationale: Boolean,
    ): CameraPermissionStatus {
        return when {
            granted -> CameraPermissionStatus.Granted
            shouldShowRationale -> CameraPermissionStatus.Denied
            else -> CameraPermissionStatus.PermanentlyDenied
        }
    }
}
