package com.arystan.arface.mask

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.arystan.arface.ui.ARViewModel

@Composable
fun MaskOverlay(vm: ARViewModel) {
    val context = LocalContext.current
    val renderer = remember { MaskRenderer(context) }
    val face by vm.faceResult.collectAsState()
    val masks by vm.masks.collectAsState()
    val activeIndex by vm.activeMaskIndex.collectAsState()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val f = face ?: return@Canvas
        val d = masks.getOrNull(activeIndex) ?: return@Canvas
        drawIntoCanvas { c ->
            renderer.render(c.nativeCanvas, size.width, size.height, f, d)
        }
    }
}
