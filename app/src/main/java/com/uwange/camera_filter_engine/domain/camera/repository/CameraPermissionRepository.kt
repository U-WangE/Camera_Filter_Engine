package com.uwange.camera_filter_engine.domain.camera.repository

import com.uwange.camera_filter_engine.domain.camera.model.CameraPermissionStatus

interface CameraPermissionRepository {
    fun resolvePermissionStatus(
        granted: Boolean,
        shouldShowRationale: Boolean,
    ): CameraPermissionStatus
}
