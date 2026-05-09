package com.arystan.arface.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.arystan.arface.face.FaceLandmarkerHelper
import com.arystan.arface.face.FaceResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManagerImpl(
    private val context: Context,
) : CameraManager {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var faceLandmarkerHelper: FaceLandmarkerHelper? = null

    @Volatile
    private var resultCallback: ((FaceResult?) -> Unit)? = null

    private var cameraProvider: ProcessCameraProvider? = null

    override fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        onFaceResult: (FaceResult?) -> Unit,
    ) {
        resultCallback = onFaceResult

        // Ensure helper is created on the calling thread (main thread expected)
        if (faceLandmarkerHelper == null) {
            faceLandmarkerHelper = FaceLandmarkerHelper(context) { result ->
                resultCallback?.invoke(result)
            }
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }

            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        android.util.Size(640, 480),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                    )
                )
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis,
                )
            } catch (e: Exception) {
                Log.e("ARFace", "CameraManagerImpl: failed to bind use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val raw = imageProxy.toBitmap()
        val timestampMs = imageProxy.imageInfo.timestamp / 1_000_000L
        val rotation = imageProxy.imageInfo.rotationDegrees
        imageProxy.close()

        // App uses DEFAULT_FRONT_CAMERA exclusively; mirror so landmarks match preview.
        val isFront = true
        val matrix = Matrix().apply {
            postRotate(rotation.toFloat())
            if (isFront) postScale(-1f, 1f, raw.width / 2f, raw.height / 2f)
        }
        val oriented = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
        raw.recycle()

        try {
            faceLandmarkerHelper?.detectAsync(oriented, timestampMs)
        } catch (e: Exception) {
            Log.w("ARFace", "CameraManagerImpl: detectAsync failed", e)
        }
    }

    override fun release() {
        resultCallback = null
        executor.shutdown()
        try {
            faceLandmarkerHelper?.close()
        } catch (e: Exception) {
            Log.w("ARFace", "CameraManagerImpl: error closing FaceLandmarkerHelper", e)
        }
        faceLandmarkerHelper = null
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.w("ARFace", "CameraManagerImpl: error unbinding camera", e)
        }
        cameraProvider = null
    }
}
