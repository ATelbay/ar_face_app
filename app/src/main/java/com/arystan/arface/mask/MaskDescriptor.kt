package com.arystan.arface.mask

import kotlinx.serialization.Serializable

/**
 * Describes a single mask. Loaded from JSON in assets/masks/ for built-ins,
 * or constructed at runtime by the AI mock client.
 *
 * Texture/thumbnail strings can be:
 *  - "asset:masks/foo.png"  → loaded from APK assets
 *  - "file:/data/.../foo.png" → loaded from internal storage (AI-generated)
 */
@Serializable
data class MaskDescriptor(
    val id: String,
    val name: String,
    val thumbnail: String,
    val layers: List<MaskLayer>,
)

@Serializable
data class MaskLayer(
    val texture: String,
    /**
     * Landmark indices that bound this layer. Bounding box = min/max over these.
     * If empty, the layer is centered on the face.
     */
    val anchorLandmarks: List<Int>,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    /**
     * Two indices [from, to]. Layer rotates by atan2 of vector(to-from).
     * Empty = no rotation.
     */
    val rotationLandmarks: List<Int> = emptyList(),
)
