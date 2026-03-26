package com.uwange.camera_filter_engine.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(CameraState())
    val state = _state.asStateFlow()

    private val _effect = Channel<CameraEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: CameraIntent) {
        when (intent) {
            CameraIntent.OnEnterScreen -> handleEnterScreen()
            is CameraIntent.OnPermissionResult -> handleCheckCameraPermission(intent.granted)
        }
    }

    private fun handleEnterScreen() {
        if (_state.value.hasCameraPermission) {
            _state.update { currentState ->
                currentState.copy(isPermissionChecked = true)
            }
            return
        }

        sendEffect(CameraEffect.RequestCameraPermission)
    }

    private fun handleCheckCameraPermission(granted: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                hasCameraPermission = granted,
                isPermissionChecked = true
            )
        }
    }

    private fun sendEffect(effect: CameraEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
