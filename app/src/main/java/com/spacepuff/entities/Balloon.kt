package com.spacepuff.entities

import com.spacepuff.engine.PhysicsEngine
import com.spacepuff.engine.Vector2D

class Balloon(
    position: Vector2D,
    radius: Float = 40f
) : GameObject(
    position = position,
    radius = radius,
    mass = 1f
) {
    companion object {
        const val MAX_AIR = 10
        const val PUFF_FORCE = 400f
        const val PUFF_COOLDOWN = 0.2f
    }

    var air: Int = MAX_AIR
        private set

    var isPopped: Boolean = false
        private set

    private var puffCooldownTimer: Float = 0f

    val facingDirection: Vector2D
        get() = Vector2D.fromAngle(rotation)

    val canPuff: Boolean
        get() = air > 0 && puffCooldownTimer <= 0f && !isPopped

    fun puff(physicsEngine: PhysicsEngine): Boolean {
        if (!canPuff) return false

        val impulse = facingDirection * -PUFF_FORCE
        velocity = physicsEngine.applyImpulse(velocity, impulse, mass)
        air--
        puffCooldownTimer = PUFF_COOLDOWN

        return true
    }

    fun refillAir(amount: Int = 2) {
        air = minOf(air + amount, MAX_AIR)
    }

    fun pop() {
        isPopped = true
        isActive = false
    }

    override fun update(physicsEngine: PhysicsEngine, deltaTime: Float) {
        super.update(physicsEngine, deltaTime)

        if (puffCooldownTimer > 0f) {
            puffCooldownTimer -= deltaTime
        }
    }

    fun reset(newPosition: Vector2D) {
        position = newPosition
        velocity = Vector2D.ZERO
        rotation = 0f
        angularVelocity = 0f
        air = MAX_AIR
        isPopped = false
        isActive = true
        puffCooldownTimer = 0f
    }
}
