package com.arystan.arface.ui

import android.util.Log
import android.view.View
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arystan.arface.ai.MockAiClient
import com.arystan.arface.camera.CameraManager
import com.arystan.arface.capture.CaptureResult
import com.arystan.arface.capture.PhotoCaptureManager
import com.arystan.arface.face.FaceResult
import com.arystan.arface.mask.MaskDescriptor
import com.arystan.arface.mask.MaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single ViewModel hub. All UI state and intents flow through here.
 *
 * Owned by orchestrator: feature agents read this signature, never edit it.
 * Bodies are intentionally minimal — feature logic lives in the manager classes
 * that the factory wires in.
 */
class ARViewModel(
    val cameraManager: CameraManager,
    val maskRepository: MaskRepository,
    val mockAiClient: MockAiClient,
    val photoCaptureManager: PhotoCaptureManager,
) : ViewModel() {

    // --- Face tracking ---
    private val _faceResult = MutableStateFlow<FaceResult?>(null)
    val faceResult: StateFlow<FaceResult?> = _faceResult.asStateFlow()

    fun onFaceResultReceived(result: FaceResult?) {
        _faceResult.value = result
    }

    // --- Masks ---
    val masks: StateFlow<List<MaskDescriptor>> =
        maskRepository.getAllMasks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = maskRepository.getAllMasks().value,
            )

    private val _activeMaskIndex = MutableStateFlow(0)
    val activeMaskIndex: StateFlow<Int> = _activeMaskIndex.asStateFlow()

    fun onMaskSelected(index: Int) {
        _activeMaskIndex.value = index
    }

    // --- Photo capture ---
    private val _captureInProgress = MutableStateFlow(false)
    val captureInProgress: StateFlow<Boolean> = _captureInProgress.asStateFlow()

    private val _lastCaptureResult = MutableStateFlow<CaptureResult?>(null)
    val lastCaptureResult: StateFlow<CaptureResult?> = _lastCaptureResult.asStateFlow()

    fun capturePhoto(view: View) {
        if (_captureInProgress.value) return
        _captureInProgress.value = true
        viewModelScope.launch {
            val result = photoCaptureManager.capture(view)
            _lastCaptureResult.value = result
            _captureInProgress.value = false
            if (result is CaptureResult.Success) {
                photoCaptureManager.share(view.context, result)
            }
        }
    }

    fun consumeCaptureResult() {
        _lastCaptureResult.value = null
    }

    // --- AI prompt dialog ---
    private val _promptDialogVisible = MutableStateFlow(false)
    val promptDialogVisible: StateFlow<Boolean> = _promptDialogVisible.asStateFlow()

    private val _aiGenerating = MutableStateFlow(false)
    val aiGenerating: StateFlow<Boolean> = _aiGenerating.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    fun showPromptDialog() {
        _aiError.value = null
        _promptDialogVisible.value = true
    }

    fun dismissPromptDialog() {
        _promptDialogVisible.value = false
    }

    fun submitPrompt(prompt: String) {
        if (prompt.isBlank() || _aiGenerating.value) return
        _aiGenerating.value = true
        _aiError.value = null
        viewModelScope.launch {
            try {
                val descriptor = mockAiClient.generateMask(prompt)
                maskRepository.addGeneratedMask(descriptor)
                val newMasks = maskRepository.getAllMasks().value
                _activeMaskIndex.value = newMasks.indexOfFirst { it.id == descriptor.id }
                    .coerceAtLeast(0)
                _promptDialogVisible.value = false
            } catch (e: Exception) {
                Log.w(TAG, "AI generation failed", e)
                _aiError.value = e.message ?: "Generation failed"
            } finally {
                _aiGenerating.value = false
            }
        }
    }

    // --- Camera lifecycle ---
    fun onCameraReady(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
    ) {
        cameraManager.bindToLifecycle(
            lifecycleOwner = lifecycleOwner,
            surfaceProvider = surfaceProvider,
            onFaceResult = ::onFaceResultReceived,
        )
    }

    override fun onCleared() {
        cameraManager.release()
        super.onCleared()
    }

    companion object {
        private const val TAG = "ARFace"
    }
}
