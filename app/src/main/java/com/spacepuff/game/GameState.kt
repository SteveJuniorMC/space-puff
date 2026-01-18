package com.spacepuff.game

enum class GameStatus {
    READY,
    PLAYING,
    GAME_OVER,
    LEVEL_COMPLETE
}

data class GameState(
    val status: GameStatus = GameStatus.READY,
    val score: Int = 0,
    val collectablesRemaining: Int = 0,
    val airRemaining: Int = 10
)
