package com.arystan.arface.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.arystan.arface.ui.ARViewModel

@Composable
fun EffectsOverlay(vm: ARViewModel) {
    val analyzer = remember { BlendshapeAnalyzer() }
    val particles = remember { ParticleSystem() }
    val face by vm.faceResult.collectAsState()

    var frameTick by remember { mutableStateOf(0L) }
    var lastNs by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { ns ->
                frameTick = ns
                val dt = if (lastNs == 0L) 0f else (ns - lastNs) / 1_000_000_000f
                lastNs = ns
                particles.tick(dt.coerceAtMost(0.05f)) // clamp big jumps
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Use the latest face read (frameTick triggers recomposition and we re-analyze)
        @Suppress("UNUSED_VARIABLE") val _trigger = frameTick
        val f = face
        val events = analyzer.analyze(f)
        events.forEach { particles.emit(it, size.width, size.height) }

        for (p in particles.particles) {
            val alpha = (p.life / p.initialLife).coerceIn(0f, 1f)
            when (p.kind) {
                ParticleKind.FIRE -> {
                    drawCircle(
                        color = Color(0xFFFF8A00).copy(alpha = alpha * 0.9f),
                        radius = p.sizePx * 0.5f,
                        center = Offset(p.x, p.y),
                    )
                    drawCircle(
                        color = Color(0xFFFFE082).copy(alpha = alpha),
                        radius = p.sizePx * 0.25f,
                        center = Offset(p.x, p.y),
                    )
                }
                ParticleKind.HEART -> {
                    val s = p.sizePx
                    val path = Path().apply {
                        moveTo(p.x, p.y + s * 0.35f)
                        cubicTo(
                            p.x - s * 0.6f, p.y - s * 0.1f,
                            p.x - s * 0.5f, p.y - s * 0.55f,
                            p.x, p.y - s * 0.2f,
                        )
                        cubicTo(
                            p.x + s * 0.5f, p.y - s * 0.55f,
                            p.x + s * 0.6f, p.y - s * 0.1f,
                            p.x, p.y + s * 0.35f,
                        )
                        close()
                    }
                    drawPath(path, color = Color(0xFFFF4081).copy(alpha = alpha))
                }
                ParticleKind.FLASH -> {
                    drawRect(
                        color = Color.White.copy(alpha = alpha),
                        topLeft = Offset(0f, 0f),
                        size = size,
                    )
                }
            }
        }
    }
}
