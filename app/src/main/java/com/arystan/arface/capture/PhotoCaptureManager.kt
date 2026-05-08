package com.arystan.arface.capture

import android.content.Context
import android.view.View

interface PhotoCaptureManager {
    suspend fun capture(rootView: View): CaptureResult
    fun share(context: Context, result: CaptureResult.Success)
}
