package com.spacepuff.entities

import com.spacepuff.engine.Vector2D

class Collectable(
    position: Vector2D,
    radius: Float = 25f,
    val points: Int = 100,
    val airRefill: Int = 2
) : GameObject(
    position = position,
    radius = radius,
    mass = 0.1f
) {
    var pulsePhase: Float = (0f..6.28f).random()
        private set

    val pulseScale: Float
        get() = 1f + 0.1f * kotlin.math.sin(pulsePhase)

    fun updatePulse(deltaTime: Float) {
        pulsePhase += deltaTime * 3f
        if (pulsePhase > 6.28f) {
            pulsePhase -= 6.28f
        }
    }

    fun collect() {
        isActive = false
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float {
    return start + (endInclusive - start) * Math.random().toFloat()
}
