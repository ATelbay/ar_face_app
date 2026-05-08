package com.arystan.arface.capture

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.content.Intent
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PhotoCaptureManagerImpl(private val appContext: Context) : PhotoCaptureManager {

    override suspend fun capture(rootView: View): CaptureResult = try {
        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
        suspendCancellableCoroutine<Unit> { cont ->
            val window = (rootView.context as? Activity)?.window
                ?: throw IllegalStateException("View not attached to Activity")
            PixelCopy.request(window, bitmap, { result ->
                if (result == PixelCopy.SUCCESS) cont.resume(Unit) {}
                else cont.resumeWithException(RuntimeException("PixelCopy failed: $result"))
            }, Handler(Looper.getMainLooper()))
        }
        val uri = saveToMediaStore(bitmap)
        bitmap.recycle()
        CaptureResult.Success(uri)
    } catch (e: Exception) {
        Log.w("ARFace", "capture failed", e)
        CaptureResult.Failure(e)
    }

    private fun saveToMediaStore(bitmap: Bitmap): Uri {
        val displayName = "ARFace_${System.currentTimeMillis()}.jpg"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/ARFace"
                )
            }
            val resolver = appContext.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw RuntimeException("MediaStore insert returned null")
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
            }
            uri
        } else {
            // API 26–28: legacy insertImage
            val uriString = MediaStore.Images.Media.insertImage(
                appContext.contentResolver,
                bitmap,
                displayName,
                "AR face capture"
            ) ?: throw RuntimeException("MediaStore.insertImage returned null")
            Uri.parse(uriString)
        }
    }

    override fun share(context: Context, result: CaptureResult.Success) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, result.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
