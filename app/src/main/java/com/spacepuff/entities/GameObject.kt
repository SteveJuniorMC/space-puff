package com.spacepuff.entities

import com.spacepuff.engine.PhysicsEngine
import com.spacepuff.engine.Vector2D

abstract class GameObject(
    var position: Vector2D,
    var velocity: Vector2D = Vector2D.ZERO,
    var rotation: Float = 0f,
    var angularVelocity: Float = 0f,
    val radius: Float,
    val mass: Float = 1f
) {
    val momentOfInertia: Float
        get() = 0.5f * mass * radius * radius

    var isActive: Boolean = true

    open fun update(physicsEngine: PhysicsEngine, deltaTime: Float) {
        if (!isActive) return

        position = physicsEngine.updatePosition(position, velocity, deltaTime)
        velocity = physicsEngine.updateVelocity(velocity)
        rotation = physicsEngine.updateRotation(rotation, angularVelocity, deltaTime)
        angularVelocity = physicsEngine.updateAngularVelocity(angularVelocity)
    }

    fun collidesWith(other: GameObject): Boolean {
        if (!isActive || !other.isActive) return false
        val dx = position.x - other.position.x
        val dy = position.y - other.position.y
        val distanceSquared = dx * dx + dy * dy
        val radiusSum = radius + other.radius
        return distanceSquared <= radiusSum * radiusSum
    }

    fun getCollisionPoint(other: GameObject): Vector2D {
        val direction = (other.position - position).normalized()
        return position + direction * radius
    }
}
