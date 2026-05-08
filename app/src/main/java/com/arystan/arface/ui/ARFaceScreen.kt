package com.arystan.arface.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arystan.arface.camera.CameraPreview
import com.arystan.arface.capture.CaptureButton
import com.arystan.arface.effects.EffectsOverlay
import com.arystan.arface.mask.MaskOverlay

/**
 * Root screen. Six slots, each filled by a feature agent's composable.
 * The orchestrator never edits this file after Wave 0.
 */
@Composable
fun ARFaceScreen(viewModel: ARViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(viewModel)
        MaskOverlay(viewModel)
        EffectsOverlay(viewModel)
        MaskGallery(viewModel)
        CaptureButton(viewModel)
        PromptDialog(viewModel)
    }
}
