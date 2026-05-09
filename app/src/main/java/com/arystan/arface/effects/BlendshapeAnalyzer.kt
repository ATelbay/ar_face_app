package com.arystan.arface.effects

import com.arystan.arface.face.FaceResult

class BlendshapeAnalyzer {
    private var prevBlinkLeft = 0f
    private var prevBlinkRight = 0f

    /** Returns events triggered by this frame's blendshapes. Mutates internal state. */
    fun analyze(face: FaceResult?): List<EffectEvent> {
        if (face == null) return emptyList()
        val events = mutableListOf<EffectEvent>()

        val jawOpen = face.blendshape("jawOpen")
        if (jawOpen > 0.4f) {
            val lm = face.landmarkOrNull(13)
            if (lm != null) events += EffectEvent.FireFromMouth(lm.x, lm.y)
        }

        val smileAvg = (face.blendshape("mouthSmileLeft") + face.blendshape("mouthSmileRight")) / 2f
        if (smileAvg > 0.5f) {
            val lCheek = face.landmarkOrNull(234)
            val rCheek = face.landmarkOrNull(454)
            if (lCheek != null && rCheek != null) {
                events += EffectEvent.HeartsFromEyes(lCheek.x, lCheek.y, rCheek.x, rCheek.y)
            }
        }

        val curBlinkLeft = face.blendshape("eyeBlinkLeft")
        val curBlinkRight = face.blendshape("eyeBlinkRight")
        // falling edge: previous score was high (open eyes had low blink), now spikes (closed)
        // Standard ARKit semantic: blink score HIGH = eye CLOSED. So edge: prev<0.15, cur>0.5
        val leftEdge = prevBlinkLeft < 0.15f && curBlinkLeft > 0.5f
        val rightEdge = prevBlinkRight < 0.15f && curBlinkRight > 0.5f
        if (leftEdge || rightEdge) {
            events += EffectEvent.ScreenFlash
        }
        prevBlinkLeft = curBlinkLeft
        prevBlinkRight = curBlinkRight
        return events
    }
}
