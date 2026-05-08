package com.arystan.arface.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arystan.arface.ui.ARViewModel

/**
 * Wave 0 stub. camera-agent replaces this file with a real CameraX PreviewView.
 * The composable signature `CameraPreview(vm)` is the orchestrator-defined contract.
 */
@Composable
fun CameraPreview(vm: ARViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    )
}
