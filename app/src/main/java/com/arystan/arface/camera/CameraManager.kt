package com.arystan.arface.camera

import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.arystan.arface.face.FaceResult

interface CameraManager {
    fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        onFaceResult: (FaceResult?) -> Unit,
    )

    fun release()
}
