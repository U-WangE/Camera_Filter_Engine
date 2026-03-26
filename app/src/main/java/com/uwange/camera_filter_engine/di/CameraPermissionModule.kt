package com.uwange.camera_filter_engine.di

import com.uwange.camera_filter_engine.data.camera.CameraPermissionRepositoryImpl
import com.uwange.camera_filter_engine.domain.camera.repository.CameraPermissionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraPermissionModule {
    @Binds
    @Singleton
    abstract fun bindCameraPermissionRepository(
        impl: CameraPermissionRepositoryImpl,
    ): CameraPermissionRepository
}
