package com.arystan.arface.effects

sealed class EffectEvent {
    data class FireFromMouth(val x: Float, val y: Float) : EffectEvent()
    data class HeartsFromEyes(
        val leftX: Float, val leftY: Float,
        val rightX: Float, val rightY: Float,
    ) : EffectEvent()
    object ScreenFlash : EffectEvent()
}
