package com.arystan.arface.effects

data class Particle(
    val kind: ParticleKind,
    var x: Float,           // pixels
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,        // 0..1, dies at 0
    val initialLife: Float, // for alpha fade
    val sizePx: Float,
)

enum class ParticleKind { FIRE, HEART, FLASH }

class ParticleSystem {
    private val _particles = mutableListOf<Particle>()
    val particles: List<Particle> get() = _particles

    fun emit(event: EffectEvent, viewWidthPx: Float, viewHeightPx: Float) {
        when (event) {
            is EffectEvent.FireFromMouth -> {
                val cx = event.x * viewWidthPx
                val cy = event.y * viewHeightPx
                repeat(8) {
                    val angle = (-Math.PI / 2 + (Math.random() - 0.5) * Math.PI * 0.6).toFloat()
                    val speed = (300f..500f).random()
                    _particles += Particle(
                        kind = ParticleKind.FIRE,
                        x = cx + (Math.random() * 20 - 10).toFloat(),
                        y = cy,
                        vx = (kotlin.math.cos(angle) * speed).toFloat(),
                        vy = (kotlin.math.sin(angle) * speed).toFloat(),
                        life = 1f, initialLife = 1f,
                        sizePx = (40f..70f).random(),
                    )
                }
            }
            is EffectEvent.HeartsFromEyes -> {
                val emitFrom = listOf(
                    event.leftX to event.leftY,
                    event.rightX to event.rightY,
                )
                emitFrom.forEach { (nx, ny) ->
                    repeat(3) {
                        val cx = nx * viewWidthPx
                        val cy = ny * viewHeightPx
                        _particles += Particle(
                            kind = ParticleKind.HEART,
                            x = cx + (Math.random() * 30 - 15).toFloat(),
                            y = cy,
                            vx = (Math.random() * 60 - 30).toFloat(),
                            vy = -(150f..250f).random(),
                            life = 1f, initialLife = 1f,
                            sizePx = (50f..80f).random(),
                        )
                    }
                }
            }
            EffectEvent.ScreenFlash -> {
                _particles += Particle(
                    kind = ParticleKind.FLASH,
                    x = 0f, y = 0f, vx = 0f, vy = 0f,
                    life = 1f, initialLife = 1f,
                    sizePx = 0f,
                )
            }
        }
    }

    /** Advance particles by dt seconds. Removes dead. */
    fun tick(dtSec: Float) {
        val iter = _particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * dtSec
            p.y += p.vy * dtSec
            // gravity for fire (rises, slows): subtract from vy magnitude
            if (p.kind == ParticleKind.FIRE) {
                p.vy += 200f * dtSec        // gravity
                p.vx *= (1f - dtSec * 0.5f) // damping
            }
            if (p.kind == ParticleKind.HEART) {
                p.vy += 50f * dtSec         // slow gravity
            }
            // flash dies fast
            val lifeDecay = if (p.kind == ParticleKind.FLASH) dtSec / 0.18f else dtSec / 0.9f
            p.life -= lifeDecay
            if (p.life <= 0f) iter.remove()
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (Math.random() * (endInclusive - start)).toFloat()
