package com.arystan.arface.capture

import android.content.Context
import android.view.View

/** Wave 0 placeholder. Replaced by PhotoCaptureManagerImpl after Wave 2 merge. */
class StubPhotoCaptureManager : PhotoCaptureManager {
    override suspend fun capture(rootView: View): CaptureResult =
        CaptureResult.Failure(IllegalStateException("Capture not yet implemented"))

    override fun share(context: Context, result: CaptureResult.Success) = Unit
}
