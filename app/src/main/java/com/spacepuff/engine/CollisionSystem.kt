package com.spacepuff.engine

import com.spacepuff.entities.Balloon
import com.spacepuff.entities.Collectable
import com.spacepuff.entities.GameObject
import com.spacepuff.entities.Obstacle
import com.spacepuff.entities.ObstacleType

sealed class CollisionResult {
    object None : CollisionResult()
    object BalloonPopped : CollisionResult()
    data class CollectableCollected(val collectable: Collectable) : CollisionResult()
    data class BounceOccurred(val obstacle: Obstacle) : CollisionResult()
}

class CollisionSystem(private val physicsEngine: PhysicsEngine) {

    fun checkBalloonCollisions(
        balloon: Balloon,
        obstacles: List<Obstacle>,
        collectables: List<Collectable>
    ): List<CollisionResult> {
        val results = mutableListOf<CollisionResult>()

        for (obstacle in obstacles) {
            if (!obstacle.isActive) continue

            if (balloon.collidesWith(obstacle)) {
                when (obstacle.type) {
                    ObstacleType.SHARP -> {
                        balloon.pop()
                        results.add(CollisionResult.BalloonPopped)
                        return results
                    }
                    ObstacleType.NEUTRAL -> {
                        handleBounceCollision(balloon, obstacle)
                        results.add(CollisionResult.BounceOccurred(obstacle))
                    }
                }
            }
        }

        for (collectable in collectables) {
            if (!collectable.isActive) continue

            if (balloon.collidesWith(collectable)) {
                collectable.collect()
                balloon.refillAir(collectable.airRefill)
                results.add(CollisionResult.CollectableCollected(collectable))
            }
        }

        return results
    }

    private fun handleBounceCollision(balloon: Balloon, obstacle: Obstacle) {
        separateObjects(balloon, obstacle)

        val (newV1, newV2) = physicsEngine.calculateElasticCollision(
            v1 = balloon.velocity,
            v2 = obstacle.velocity,
            m1 = balloon.mass,
            m2 = obstacle.mass,
            x1 = balloon.position,
            x2 = obstacle.position
        )

        val collisionPoint = balloon.getCollisionPoint(obstacle)
        val impulse = (newV1 - balloon.velocity) * balloon.mass

        balloon.velocity = newV1
        balloon.angularVelocity = physicsEngine.applyAngularImpulse(
            angularVelocity = balloon.angularVelocity,
            collisionPoint = collisionPoint,
            center = balloon.position,
            impulse = impulse,
            momentOfInertia = balloon.momentOfInertia
        )

        obstacle.velocity = newV2
    }

    private fun separateObjects(obj1: GameObject, obj2: GameObject) {
        val direction = (obj1.position - obj2.position).normalized()
        val overlap = (obj1.radius + obj2.radius) -
                (obj1.position - obj2.position).magnitude()

        if (overlap > 0) {
            val totalMass = obj1.mass + obj2.mass
            val ratio1 = obj2.mass / totalMass
            val ratio2 = obj1.mass / totalMass

            obj1.position = obj1.position + direction * (overlap * ratio1 + 1f)
            obj2.position = obj2.position - direction * (overlap * ratio2 + 1f)
        }
    }

    fun checkBoundaryCollision(
        obj: GameObject,
        screenWidth: Float,
        screenHeight: Float,
        padding: Float = 0f
    ): Boolean {
        val minX = padding + obj.radius
        val maxX = screenWidth - padding - obj.radius
        val minY = padding + obj.radius
        val maxY = screenHeight - padding - obj.radius

        var collided = false

        if (obj.position.x < minX) {
            obj.position.x = minX
            obj.velocity.x = -obj.velocity.x * 0.8f
            collided = true
        } else if (obj.position.x > maxX) {
            obj.position.x = maxX
            obj.velocity.x = -obj.velocity.x * 0.8f
            collided = true
        }

        if (obj.position.y < minY) {
            obj.position.y = minY
            obj.velocity.y = -obj.velocity.y * 0.8f
            collided = true
        } else if (obj.position.y > maxY) {
            obj.position.y = maxY
            obj.velocity.y = -obj.velocity.y * 0.8f
            collided = true
        }

        return collided
    }
}
