package com.uwange.camera_filter_engine.presentation.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.net.Uri
import android.opengl.GLSurfaceView
import android.provider.Settings
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.uwange.camera_filter_engine.R
import com.uwange.camera_filter_engine.domain.camera.model.CameraPermissionStatus
import com.uwange.camera_filter_engine.domain.camera.model.FilterType
import com.uwange.camera_filter_engine.presentation.camera.gl.CameraRenderer
import com.uwange.camera_filter_engine.ui.theme.component.ConfirmDialog
import kotlinx.coroutines.flow.filterNotNull

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
        onFilterToggle = {
            val next = if (state.filterType == FilterType.NONE) FilterType.GRAYSCALE else FilterType.NONE
            viewModel.onIntent(CameraIntent.SelectFilter(next))
        },
    )
}

@Composable
fun CameraScreen(
    state: CameraState,
    onRetryPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onFilterToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state.permissionStatus) {
            CameraPermissionStatus.Idle -> Unit
            CameraPermissionStatus.Granted -> {
                CameraPreview(filterType = state.filterType)
                Button(
                    onClick = onFilterToggle,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                ) {
                    Text(if (state.filterType == FilterType.NONE) "Grayscale" else "Normal")
                }
            }
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
    filterType: FilterType = FilterType.NONE,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val renderer = remember {
        CameraRenderer()
    }

    LaunchedEffect(filterType) {
        renderer.setFilter(filterType)
    }

    LaunchedEffect(renderer) {
        renderer.surfaceTexture
            .filterNotNull()
            .collect { surfaceTexture ->
                bindCamera(context, lifecycleOwner, surfaceTexture)
            }
    }

    AndroidView(
        factory = {
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(2)
                setRenderer(renderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
        },
        modifier = modifier
    )
}

private fun bindCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    surfaceTexture: SurfaceTexture,
) {
    val executor = ContextCompat.getMainExecutor(context)
    ProcessCameraProvider.getInstance(context).addListener({
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val surface = Surface(surfaceTexture)

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider { request ->
                surfaceTexture.setDefaultBufferSize(
                    request.resolution.width,
                    request.resolution.height,
                )
                request.provideSurface(surface, executor) {
                    surface.release()
                }
            }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
        )
    }, executor)
}
