package com.arystan.arface.ai

import com.arystan.arface.mask.MaskDescriptor
import com.arystan.arface.mask.MaskLayer
import kotlinx.coroutines.delay
import java.util.UUID

/** Wave 0 placeholder. Replaced by MockAiClientImpl after Wave 3 merge. */
class StubMockAiClient : MockAiClient {
    override suspend fun generateMask(prompt: String): MaskDescriptor {
        delay(300)
        val id = UUID.randomUUID().toString()
        return MaskDescriptor(
            id = id,
            name = prompt.take(20).ifBlank { "stub" },
            thumbnail = "asset:masks/cat_ears_thumb.png",
            layers = listOf(
                MaskLayer(
                    texture = "asset:masks/cat_ears_layer0.png",
                    anchorLandmarks = listOf(1, 4, 6, 168, 195, 197),
                ),
            ),
        )
    }
}
