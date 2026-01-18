package com.spacepuff.engine

import kotlin.math.sqrt

data class Vector2D(
    var x: Float = 0f,
    var y: Float = 0f
) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vector2D(x / scalar, y / scalar)

    fun dot(other: Vector2D): Float = x * other.x + y * other.y

    fun cross(other: Vector2D): Float = x * other.y - y * other.x

    fun magnitude(): Float = sqrt(x * x + y * y)

    fun magnitudeSquared(): Float = x * x + y * y

    fun normalized(): Vector2D {
        val mag = magnitude()
        return if (mag > 0f) this / mag else Vector2D(0f, 0f)
    }

    fun rotated(angleRadians: Float): Vector2D {
        val cos = kotlin.math.cos(angleRadians)
        val sin = kotlin.math.sin(angleRadians)
        return Vector2D(
            x * cos - y * sin,
            x * sin + y * cos
        )
    }

    companion object {
        val ZERO = Vector2D(0f, 0f)
        fun fromAngle(angleRadians: Float): Vector2D {
            return Vector2D(
                kotlin.math.cos(angleRadians),
                kotlin.math.sin(angleRadians)
            )
        }
    }
}

class PhysicsEngine {
    fun updatePosition(
        position: Vector2D,
        velocity: Vector2D,
        deltaTime: Float
    ): Vector2D {
        return position + velocity * deltaTime
    }

    fun updateRotation(rotation: Float, angularVelocity: Float, deltaTime: Float): Float {
        return rotation + angularVelocity * deltaTime
    }

    fun applyImpulse(velocity: Vector2D, impulse: Vector2D, mass: Float): Vector2D {
        return velocity + impulse / mass
    }

    fun applyAngularImpulse(
        angularVelocity: Float,
        collisionPoint: Vector2D,
        center: Vector2D,
        impulse: Vector2D,
        momentOfInertia: Float
    ): Float {
        val r = collisionPoint - center
        val torque = r.cross(impulse)
        return angularVelocity + torque / momentOfInertia
    }

    fun calculateElasticCollision(
        v1: Vector2D,
        v2: Vector2D,
        m1: Float,
        m2: Float,
        x1: Vector2D,
        x2: Vector2D,
        restitution: Float = 0.8f
    ): Pair<Vector2D, Vector2D> {
        val dx = x1 - x2
        val dxMagSq = dx.magnitudeSquared()

        if (dxMagSq == 0f) return Pair(v1, v2)

        val dv = v1 - v2
        val dvDotDx = dv.dot(dx)

        val massRatio1 = 2f * m2 / (m1 + m2)
        val massRatio2 = 2f * m1 / (m1 + m2)

        val scalar1 = massRatio1 * dvDotDx / dxMagSq * restitution
        val scalar2 = massRatio2 * dvDotDx / dxMagSq * restitution

        val v1New = v1 - dx * scalar1
        val v2New = v2 + dx * scalar2

        return Pair(v1New, v2New)
    }
}
