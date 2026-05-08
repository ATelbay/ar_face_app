package com.arystan.arface.face

data class Landmark(
    val x: Float,
    val y: Float,
    val z: Float = 0f,
)

data class Blendshape(
    val name: String,
    val score: Float,
)

data class FaceResult(
    val landmarks: List<Landmark>,
    val blendshapes: List<Blendshape>,
    val transformMatrix: FloatArray,
    val imageWidth: Int,
    val imageHeight: Int,
    val timestampMs: Long,
) {
    fun blendshape(name: String): Float =
        blendshapes.firstOrNull { it.name == name }?.score ?: 0f

    fun landmarkOrNull(index: Int): Landmark? =
        landmarks.getOrNull(index)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FaceResult) return false
        return timestampMs == other.timestampMs &&
            imageWidth == other.imageWidth &&
            imageHeight == other.imageHeight &&
            landmarks == other.landmarks &&
            blendshapes == other.blendshapes &&
            transformMatrix.contentEquals(other.transformMatrix)
    }

    override fun hashCode(): Int {
        var h = landmarks.hashCode()
        h = 31 * h + blendshapes.hashCode()
        h = 31 * h + transformMatrix.contentHashCode()
        h = 31 * h + imageWidth
        h = 31 * h + imageHeight
        h = 31 * h + timestampMs.hashCode()
        return h
    }
}
