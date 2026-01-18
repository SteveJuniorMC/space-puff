package com.spacepuff.entities

import com.spacepuff.engine.Vector2D

enum class ObstacleType {
    SHARP,
    NEUTRAL
}

class Obstacle(
    position: Vector2D,
    radius: Float,
    val type: ObstacleType,
    velocity: Vector2D = Vector2D.ZERO,
    mass: Float = 5f
) : GameObject(
    position = position,
    velocity = velocity,
    radius = radius,
    mass = mass
) {
    val isSharp: Boolean
        get() = type == ObstacleType.SHARP

    val spikeCount: Int = when (type) {
        ObstacleType.SHARP -> (4..8).random()
        ObstacleType.NEUTRAL -> 0
    }
}
