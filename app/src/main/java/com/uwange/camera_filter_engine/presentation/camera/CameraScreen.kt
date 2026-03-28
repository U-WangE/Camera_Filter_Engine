package com.uwange.camera_filter_engine.presentation.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.uwange.camera_filter_engine.R
import com.uwange.camera_filter_engine.domain.camera.model.CameraPermissionStatus
import com.uwange.camera_filter_engine.ui.theme.component.ConfirmDialog

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraRoute(viewModel: CameraViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { granted ->
            val shouldShowRationale = !granted &&
                (context as? Activity)?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true

            viewModel.onIntent(
                CameraIntent.PermissionResult(
                    granted = granted,
                    shouldShowRationale = shouldShowRationale,
                )
            )
        }
    )

    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.onIntent(
                CameraIntent.PermissionResult(
                    granted = true,
                    shouldShowRationale = false,
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            viewModel.onIntent(CameraIntent.RequestPermission)
        }

        viewModel.effect.collect { effect ->
            when (effect) {
                CameraEffect.LaunchPermissionRequest -> cameraPermissionState.launchPermissionRequest()
                CameraEffect.OpenAppSettings -> {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            }
        }
    }

    CameraScreen(
        state = state,
        onRetryPermission = { viewModel.onIntent(CameraIntent.RequestPermission) },
        onOpenSettings = { viewModel.onIntent(CameraIntent.OpenAppSettings) },
    )
}

@Composable
fun CameraScreen(
    state: CameraState,
    onRetryPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state.permissionStatus) {
            CameraPermissionStatus.Idle -> Unit
            CameraPermissionStatus.Granted -> CameraPreview()
            CameraPermissionStatus.Denied -> {
                ConfirmDialog(
                    title = stringResource(R.string.camera_permission_title),
                    message = stringResource(R.string.camera_permission_denied_message),
                    confirmText = stringResource(R.string.camera_permission_retry),
                    onConfirmClick = onRetryPermission
                )
            }

            CameraPermissionStatus.PermanentlyDenied -> {
                ConfirmDialog(
                    title = stringResource(R.string.camera_permission_title),
                    message = stringResource(R.string.camera_permission_permanently_denied_message),
                    confirmText = stringResource(R.string.camera_permission_open_settings),
                    onConfirmClick = onOpenSettings
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context)
    }
    val preview = remember {
        Preview.Builder().build()
    }

    DisposableEffect(lifecycleOwner, context) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        preview.surfaceProvider = previewView.surfaceProvider

        val selector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            selector,
            preview
        )

        onDispose {
            cameraProvider.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
