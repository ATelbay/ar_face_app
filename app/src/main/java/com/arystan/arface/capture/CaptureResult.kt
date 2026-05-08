package com.arystan.arface.capture

import android.net.Uri

sealed class CaptureResult {
    data class Success(val uri: Uri) : CaptureResult()
    data class Failure(val error: Throwable) : CaptureResult()
}
