package com.arystan.arface.mask

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import com.arystan.arface.face.FaceResult

class MaskRenderer(private val context: Context) {

    private val bitmapCache = mutableMapOf<String, Bitmap>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    fun render(
        canvas: android.graphics.Canvas,
        viewWidthPx: Float,
        viewHeightPx: Float,
        face: FaceResult,
        descriptor: MaskDescriptor,
    ) {
        for (layer in descriptor.layers) {
            try {
                renderLayer(canvas, viewWidthPx, viewHeightPx, face, layer)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to render layer ${layer.texture}", e)
            }
        }
    }

    private fun renderLayer(
        canvas: android.graphics.Canvas,
        viewWidthPx: Float,
        viewHeightPx: Float,
        face: FaceResult,
        layer: MaskLayer,
    ) {
        val bmp = resolveBitmap(layer.texture) ?: return

        // Compute face bounding box using indices [10, 152, 234, 454]
        val faceBboxIndices = listOf(10, 152, 234, 454)
        val bboxLandmarks = if (faceBboxIndices.all { it < face.landmarks.size }) {
            faceBboxIndices.map { face.landmarks[it] }
        } else {
            face.landmarks
        }

        val faceMinX = bboxLandmarks.minOf { it.x }
        val faceMaxX = bboxLandmarks.maxOf { it.x }
        val faceMinY = bboxLandmarks.minOf { it.y }
        val faceMaxY = bboxLandmarks.maxOf { it.y }

        // Compute layer center from anchorLandmarks
        if (layer.anchorLandmarks.any { it >= face.landmarks.size }) return
        val anchorPoints = layer.anchorLandmarks.map { face.landmarks[it] }
        val layerCenterX = anchorPoints.map { it.x }.average().toFloat()
        val layerCenterY = anchorPoints.map { it.y }.average().toFloat()

        // Apply FILL_CENTER inverse transform: map landmark norms to view pixels
        val imgW = face.imageWidth.toFloat()
        val imgH = face.imageHeight.toFloat()
        val scale = maxOf(viewWidthPx / imgW, viewHeightPx / imgH)
        val scaledImgW = imgW * scale
        val scaledImgH = imgH * scale
        val imgLeftPx = (viewWidthPx - scaledImgW) / 2f
        val imgTopPx  = (viewHeightPx - scaledImgH) / 2f

        var centerXpx = imgLeftPx + layerCenterX * scaledImgW
        var centerYpx = imgTopPx  + layerCenterY * scaledImgH
        val faceWidthPx  = (faceMaxX - faceMinX) * scaledImgW
        val faceHeightPx = (faceMaxY - faceMinY) * scaledImgH

        // Apply offsets
        centerXpx += layer.offsetX * faceWidthPx
        centerYpx += layer.offsetY * faceHeightPx

        // Draw size
        val drawWidth = faceWidthPx * layer.widthScale
        val drawHeight = if (layer.keepAspect) drawWidth * (bmp.height.toFloat() / bmp.width.toFloat())
                         else faceHeightPx * layer.heightScale

        // Rotation
        val angleDeg = if (layer.rotationLandmarks.size == 2) {
            val p0 = face.landmarks[layer.rotationLandmarks[0]]
            val p1 = face.landmarks[layer.rotationLandmarks[1]]
            Math.toDegrees(
                Math.atan2(
                    (p1.y - p0.y).toDouble(),
                    (p1.x - p0.x).toDouble()
                )
            ).toFloat()
        } else {
            0f
        }

        // Draw
        val m = Matrix()
        m.postTranslate(-bmp.width / 2f, -bmp.height / 2f)
        m.postScale(drawWidth / bmp.width, drawHeight / bmp.height)
        m.postRotate(angleDeg)
        m.postTranslate(centerXpx, centerYpx)
        canvas.drawBitmap(bmp, m, paint)
    }

    private fun resolveBitmap(texturePath: String): Bitmap? {
        bitmapCache[texturePath]?.let { return it }

        val bmp = when {
            texturePath.startsWith("asset:") -> {
                val path = texturePath.removePrefix("asset:")
                try {
                    context.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load asset: $path", e)
                    null
                }
            }
            texturePath.startsWith("file:") -> {
                val path = texturePath.removePrefix("file:")
                try {
                    BitmapFactory.decodeFile(path)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load file: $path", e)
                    null
                }
            }
            else -> {
                Log.w(TAG, "Unknown texture path scheme: $texturePath")
                null
            }
        }

        if (bmp != null) {
            bitmapCache[texturePath] = bmp
        }
        return bmp
    }

    companion object {
        private const val TAG = "MaskRenderer"
    }
}
