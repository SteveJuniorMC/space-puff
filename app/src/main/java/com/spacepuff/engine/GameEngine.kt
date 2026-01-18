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

    private var accumulator: Float = 0f

    fun initialize(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
        resetGame()
    }

    fun resetGame() {
        val level = levelGenerator.generateLevel(screenWidth, screenHeight)
        balloon = level.balloon
        obstacles = level.obstacles
        collectables = level.collectables

        gameState = GameState(
            status = GameStatus.READY,
            score = 0,
            collectablesRemaining = collectables.size,
            airRemaining = balloon.air
        )
        accumulator = 0f
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

        collisionSystem.checkBoundaryCollision(balloon, screenWidth, screenHeight)
        obstacles.forEach {
            collisionSystem.checkBoundaryCollision(it, screenWidth, screenHeight)
        }

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
