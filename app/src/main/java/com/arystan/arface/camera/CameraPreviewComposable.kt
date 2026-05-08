package com.arystan.arface.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arystan.arface.ui.ARViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext

@Composable
fun CameraPreview(vm: ARViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        update = { previewView ->
            vm.onCameraReady(lifecycleOwner, previewView.surfaceProvider)
        },
    )

    DisposableEffect(Unit) {
        onDispose { /* release happens via VM.onCleared */ }
    }
}
