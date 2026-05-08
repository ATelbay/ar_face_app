package com.arystan.arface.face

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker.FaceLandmarkerOptions
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceLandmarkerHelper(
    context: Context,
    private val onResult: (FaceResult?) -> Unit,
) {

    private val faceLandmarker: FaceLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("face_landmarker.task")
            .build()

        val options = FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.5f)
            .setMinFacePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setOutputFaceBlendshapes(true)
            .setOutputFacialTransformationMatrixes(true)
            .setResultListener { result, input -> handleResult(result, input) }
            .setErrorListener { e -> Log.w("ARFace", "Landmarker error", e) }
            .build()

        faceLandmarker = FaceLandmarker.createFromOptions(context, options)
    }

    fun detectAsync(bitmap: Bitmap, timestampMs: Long) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        faceLandmarker.detectAsync(mpImage, timestampMs)
    }

    private fun handleResult(result: FaceLandmarkerResult, input: MPImage) {
        if (result.faceLandmarks().isEmpty()) {
            onResult(null)
            return
        }

        val rawLandmarks = result.faceLandmarks()[0]
        val landmarks = rawLandmarks.map { lm ->
            Landmark(
                x = 1f - lm.x(),  // Mirror x for front camera selfie
                y = lm.y(),
                z = lm.z(),
            )
        }

        val blendshapes: List<Blendshape> = if (result.faceBlendshapes().isPresent &&
            result.faceBlendshapes().get().isNotEmpty()
        ) {
            result.faceBlendshapes().get()[0].map { category ->
                Blendshape(
                    name = category.categoryName(),
                    score = category.score(),
                )
            }
        } else {
            emptyList()
        }

        // facialTransformationMatrixes() returns Optional<List<float[]>> where each float[] is 16 elements
        val transformMatrix: FloatArray = if (result.facialTransformationMatrixes().isPresent &&
            result.facialTransformationMatrixes().get().isNotEmpty()
        ) {
            result.facialTransformationMatrixes().get()[0]
        } else {
            FloatArray(16)
        }

        val faceResult = FaceResult(
            landmarks = landmarks,
            blendshapes = blendshapes,
            transformMatrix = transformMatrix,
            imageWidth = input.width,
            imageHeight = input.height,
            timestampMs = System.currentTimeMillis(),
        )

        onResult(faceResult)
    }

    fun close() {
        faceLandmarker.close()
    }
}
