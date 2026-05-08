package com.arystan.arface.camera

import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.arystan.arface.face.FaceResult

/** Wave 0 placeholder. Replaced by [CameraManagerImpl] after Wave 1 merge. */
class StubCameraManager : CameraManager {
    override fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        onFaceResult: (FaceResult?) -> Unit,
    ) {
        onFaceResult(null)
    }

    override fun release() = Unit
}
