package com.spacepuff.engine

import com.spacepuff.entities.Balloon
import com.spacepuff.entities.Collectable
import com.spacepuff.entities.Obstacle
import com.spacepuff.game.GameState
import com.spacepuff.game.GameStatus
import com.spacepuff.game.LevelGenerator

class GameEngine {
    companion object {
        const val FIXED_TIMESTEP = 1f / 60f
        const val MAX_FRAME_TIME = 0.25f
        const val WORLD_SCALE = 3f // World is 3x screen size
    }

    private val physicsEngine = PhysicsEngine()
    private val collisionSystem = CollisionSystem(physicsEngine)
    private val levelGenerator = LevelGenerator()

    var balloon: Balloon = Balloon(Vector2D(100f, 100f))
        private set

    var obstacles: List<Obstacle> = emptyList()
        private set

    var collectables: List<Collectable> = emptyList()
        private set

    var gameState: GameState = GameState()
        private set

    var screenWidth: Float = 0f
        private set

    var screenHeight: Float = 0f
        private set

    var worldWidth: Float = 0f
        private set

    var worldHeight: Float = 0f
        private set

    // Camera position (top-left corner of viewport)
    var cameraX: Float = 0f
        private set

    var cameraY: Float = 0f
        private set

    private var accumulator: Float = 0f

    fun initialize(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
        worldWidth = width * WORLD_SCALE
        worldHeight = height * WORLD_SCALE
        resetGame()
    }

    fun resetGame() {
        val level = levelGenerator.generateLevel(worldWidth, worldHeight)
        balloon = level.balloon
        obstacles = level.obstacles
        collectables = level.collectables

        updateCamera()

        gameState = GameState(
            status = GameStatus.READY,
            score = 0,
            collectablesRemaining = collectables.size,
            airRemaining = balloon.air
        )
        accumulator = 0f
    }

    private fun updateCamera() {
        // Center camera on balloon
        cameraX = balloon.position.x - screenWidth / 2f
        cameraY = balloon.position.y - screenHeight / 2f

        // Clamp to world bounds
        cameraX = cameraX.coerceIn(0f, worldWidth - screenWidth)
        cameraY = cameraY.coerceIn(0f, worldHeight - screenHeight)
    }

    fun startGame() {
        if (gameState.status == GameStatus.READY) {
            gameState = gameState.copy(status = GameStatus.PLAYING)
        }
    }

    fun update(deltaTime: Float) {
        if (gameState.status != GameStatus.PLAYING) return

        val frameTime = minOf(deltaTime, MAX_FRAME_TIME)
        accumulator += frameTime

        while (accumulator >= FIXED_TIMESTEP) {
            physicsStep(FIXED_TIMESTEP)
            accumulator -= FIXED_TIMESTEP
        }

        collectables.forEach { it.updatePulse(deltaTime) }

        updateGameState()
    }

    private fun physicsStep(dt: Float) {
        balloon.update(physicsEngine, dt)

        obstacles.forEach { it.update(physicsEngine, dt) }

        // Use world boundaries instead of screen
        collisionSystem.checkBoundaryCollision(balloon, worldWidth, worldHeight)
        obstacles.forEach {
            collisionSystem.checkBoundaryCollision(it, worldWidth, worldHeight)
        }

        updateCamera()

        val results = collisionSystem.checkBalloonCollisions(balloon, obstacles, collectables)

        for (result in results) {
            when (result) {
                is CollisionResult.BalloonPopped -> {
                    gameState = gameState.copy(status = GameStatus.GAME_OVER)
                    return
                }
                is CollisionResult.CollectableCollected -> {
                    gameState = gameState.copy(
                        score = gameState.score + result.collectable.points
                    )
                }
                is CollisionResult.BounceOccurred -> {}
                CollisionResult.None -> {}
            }
        }
    }

    private fun updateGameState() {
        val activeCollectables = collectables.count { it.isActive }

        gameState = gameState.copy(
            collectablesRemaining = activeCollectables,
            airRemaining = balloon.air
        )

        if (activeCollectables == 0 && gameState.status == GameStatus.PLAYING) {
            gameState = gameState.copy(status = GameStatus.LEVEL_COMPLETE)
        }

        if (balloon.air <= 0 && balloon.velocity.magnitudeSquared() < 1f) {
            gameState = gameState.copy(status = GameStatus.GAME_OVER)
        }
    }

    fun puff(): Boolean {
        if (gameState.status != GameStatus.PLAYING) return false

        val success = balloon.puff(physicsEngine)
        if (success) {
            gameState = gameState.copy(airRemaining = balloon.air)
        }
        return success
    }

    fun getInterpolationAlpha(): Float {
        return accumulator / FIXED_TIMESTEP
    }
}
