package com.arystan.arface.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arystan.arface.ai.MockAiClientImpl
import com.arystan.arface.camera.CameraManagerImpl
import com.arystan.arface.capture.PhotoCaptureManagerImpl
import com.arystan.arface.mask.MaskRepositoryImpl

/**
 * Constructs [ARViewModel] with concrete implementations. The orchestrator
 * updates this file after each wave merge by swapping a Stub* for the real
 * impl created on the merged feature branch.
 *
 * Wave 0: all stubs.
 * After Wave 1: CameraManagerImpl.
 * After Wave 2: MaskRepositoryImpl, PhotoCaptureManagerImpl.
 * After Wave 3: MockAiClientImpl.
 */
class ARViewModelFactory(
    private val appContext: Context,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ARViewModel::class.java)) {
            "Unknown ViewModel: $modelClass"
        }
        return ARViewModel(
            cameraManager = CameraManagerImpl(appContext),
            maskRepository = MaskRepositoryImpl(appContext),
            mockAiClient = MockAiClientImpl(appContext),
            photoCaptureManager = PhotoCaptureManagerImpl(appContext),
        ) as T
    }
}
