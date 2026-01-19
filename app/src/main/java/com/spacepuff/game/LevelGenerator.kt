package com.spacepuff.game

import com.spacepuff.engine.Vector2D
import com.spacepuff.entities.Balloon
import com.spacepuff.entities.Collectable
import com.spacepuff.entities.Obstacle
import com.spacepuff.entities.ObstacleType
import kotlin.random.Random

data class Level(
    val balloon: Balloon,
    val obstacles: List<Obstacle>,
    val collectables: List<Collectable>
)

class LevelGenerator {
    companion object {
        const val BALLOON_RADIUS = 40f
        const val MIN_OBSTACLE_RADIUS = 30f
        const val MAX_OBSTACLE_RADIUS = 60f
        const val COLLECTABLE_RADIUS = 25f
        const val MIN_SPAWN_DISTANCE = 100f
        const val SHARP_OBSTACLE_COUNT = 12
        const val NEUTRAL_OBSTACLE_COUNT = 10
        const val COLLECTABLE_COUNT = 15
    }

    fun generateLevel(screenWidth: Float, screenHeight: Float): Level {
        val spawnedPositions = mutableListOf<Pair<Vector2D, Float>>()

        val balloonPos = Vector2D(
            screenWidth * 0.15f,
            screenHeight * 0.5f
        )
        val balloon = Balloon(balloonPos, BALLOON_RADIUS)
        balloon.velocity = randomVelocity(30f, 60f)
        balloon.angularVelocity = randomAngularVelocity()
        spawnedPositions.add(balloonPos to BALLOON_RADIUS + MIN_SPAWN_DISTANCE)

        val obstacles = mutableListOf<Obstacle>()

        repeat(SHARP_OBSTACLE_COUNT) {
            val radius = Random.nextFloat() * (MAX_OBSTACLE_RADIUS - MIN_OBSTACLE_RADIUS) + MIN_OBSTACLE_RADIUS
            val position = findValidPosition(
                screenWidth, screenHeight, radius, spawnedPositions
            )
            if (position != null) {
                val obstacle = Obstacle(
                    position = position,
                    radius = radius,
                    type = ObstacleType.SHARP,
                    velocity = randomVelocity(20f, 50f)
                )
                obstacle.angularVelocity = randomAngularVelocity()
                obstacles.add(obstacle)
                spawnedPositions.add(position to radius + MIN_SPAWN_DISTANCE / 2)
            }
        }

        repeat(NEUTRAL_OBSTACLE_COUNT) {
            val radius = Random.nextFloat() * (MAX_OBSTACLE_RADIUS - MIN_OBSTACLE_RADIUS) + MIN_OBSTACLE_RADIUS
            val position = findValidPosition(
                screenWidth, screenHeight, radius, spawnedPositions
            )
            if (position != null) {
                val obstacle = Obstacle(
                    position = position,
                    radius = radius,
                    type = ObstacleType.NEUTRAL,
                    velocity = randomVelocity(20f, 50f)
                )
                obstacle.angularVelocity = randomAngularVelocity()
                obstacles.add(obstacle)
                spawnedPositions.add(position to radius + MIN_SPAWN_DISTANCE / 2)
            }
        }

        val collectables = mutableListOf<Collectable>()

        repeat(COLLECTABLE_COUNT) {
            val position = findValidPosition(
                screenWidth, screenHeight, COLLECTABLE_RADIUS, spawnedPositions
            )
            if (position != null) {
                collectables.add(Collectable(position, COLLECTABLE_RADIUS))
                spawnedPositions.add(position to COLLECTABLE_RADIUS + MIN_SPAWN_DISTANCE / 2)
            }
        }

        return Level(balloon, obstacles, collectables)
    }

    private fun randomVelocity(minSpeed: Float, maxSpeed: Float): Vector2D {
        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val speed = Random.nextFloat() * (maxSpeed - minSpeed) + minSpeed
        return Vector2D(
            kotlin.math.cos(angle) * speed,
            kotlin.math.sin(angle) * speed
        )
    }

    private fun randomAngularVelocity(): Float {
        // Avoid slow rotation (-50 to +50 degrees/sec ≈ -0.87 to +0.87 rad/sec)
        val speed = Random.nextFloat() * 0.63f + 0.87f  // 0.87 to 1.5 rad/sec
        return if (Random.nextBoolean()) speed else -speed
    }

    private fun findValidPosition(
        screenWidth: Float,
        screenHeight: Float,
        radius: Float,
        existingPositions: List<Pair<Vector2D, Float>>,
        maxAttempts: Int = 50
    ): Vector2D? {
        val padding = radius + 20f

        repeat(maxAttempts) {
            val x = Random.nextFloat() * (screenWidth - 2 * padding) + padding
            val y = Random.nextFloat() * (screenHeight - 2 * padding) + padding
            val candidate = Vector2D(x, y)

            val isValid = existingPositions.all { (pos, minDist) ->
                val dx = candidate.x - pos.x
                val dy = candidate.y - pos.y
                val distSq = dx * dx + dy * dy
                distSq >= (radius + minDist) * (radius + minDist)
            }

            if (isValid) return candidate
        }

        return null
    }
}
