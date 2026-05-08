package com.arystan.arface.mask

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MaskRepositoryImpl(context: Context) : MaskRepository {

    private val builtIns = listOf(
        MaskDescriptor(
            id = "cat_ears",
            name = "Котик",
            thumbnail = "asset:masks/cat_ears_thumb.png",
            layers = listOf(
                MaskLayer(
                    texture = "asset:masks/cat_ears_layer0.png",
                    anchorLandmarks = listOf(10, 109, 67, 297, 338),
                    offsetY = -0.45f,
                    scaleX = 1.4f, scaleY = 1.4f,
                    rotationLandmarks = listOf(234, 454),
                ),
            ),
        ),
        MaskDescriptor(
            id = "sunglasses_hat",
            name = "Шляпа+очки",
            thumbnail = "asset:masks/sunglasses_thumb.png",
            layers = listOf(
                MaskLayer(
                    texture = "asset:masks/sunglasses_layer0.png",
                    anchorLandmarks = listOf(33, 133, 362, 263),
                    scaleX = 1.4f, scaleY = 1.6f,
                    rotationLandmarks = listOf(33, 263),
                ),
                MaskLayer(
                    texture = "asset:masks/hat_layer0.png",
                    anchorLandmarks = listOf(10, 109, 338),
                    offsetY = -0.55f,
                    scaleX = 1.6f, scaleY = 1.2f,
                    rotationLandmarks = listOf(234, 454),
                ),
            ),
        ),
        MaskDescriptor(
            id = "alien",
            name = "Пришелец",
            thumbnail = "asset:masks/alien_thumb.png",
            layers = listOf(
                MaskLayer(
                    texture = "asset:masks/alien_layer0.png",
                    anchorLandmarks = listOf(10, 152, 234, 454),
                    scaleX = 1.1f, scaleY = 1.15f,
                    rotationLandmarks = listOf(234, 454),
                ),
            ),
        ),
        MaskDescriptor(
            id = "makeup",
            name = "Макияж",
            thumbnail = "asset:masks/makeup_thumb.png",
            layers = listOf(
                MaskLayer(
                    texture = "asset:masks/makeup_layer0.png",
                    anchorLandmarks = listOf(10, 152, 234, 454),
                    scaleX = 1.05f, scaleY = 1.1f,
                    rotationLandmarks = listOf(234, 454),
                ),
            ),
        ),
    )

    private val _masks = MutableStateFlow<List<MaskDescriptor>>(builtIns)

    override fun getAllMasks(): StateFlow<List<MaskDescriptor>> = _masks

    override fun addGeneratedMask(descriptor: MaskDescriptor) {
        _masks.value = _masks.value + descriptor
    }
}
