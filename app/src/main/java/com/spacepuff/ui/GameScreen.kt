package com.spacepuff.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacepuff.engine.GameEngine
import com.spacepuff.game.GameStatus
import com.spacepuff.ui.theme.SpacePuffColors

@Composable
fun GameScreen() {
    val gameEngine = remember { GameEngine() }
    var gameState by remember { mutableStateOf(gameEngine.gameState) }
    var isInitialized by remember { mutableStateOf(false) }
    var frameCount by remember { mutableStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpacePuffColors.SpaceBackground)
            .onSizeChanged { size ->
                if (!isInitialized && size.width > 0 && size.height > 0) {
                    gameEngine.initialize(size.width.toFloat(), size.height.toFloat())
                    gameState = gameEngine.gameState
                    isInitialized = true
                }
            }
    ) {
        if (isInitialized) {
            LaunchedEffect(gameState.status) {
                if (gameState.status == GameStatus.PLAYING) {
                    var lastTime = withFrameNanos { it }
                    while (gameState.status == GameStatus.PLAYING) {
                        val currentTime = withFrameNanos { it }
                        val deltaTime = (currentTime - lastTime) / 1_000_000_000f
                        lastTime = currentTime
                        gameEngine.update(deltaTime)
                        gameState = gameEngine.gameState
                        frameCount++
                    }
                }
            }

            // frameCount forces recomposition each frame
            key(frameCount) {
                GameCanvas(
                    balloon = gameEngine.balloon,
                    obstacles = gameEngine.obstacles,
                    collectables = gameEngine.collectables,
                    modifier = Modifier.fillMaxSize()
                )
            }

            GameHUD(
                score = gameState.score,
                air = gameState.airRemaining,
                collectablesRemaining = gameState.collectablesRemaining,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            )

            if (gameState.status == GameStatus.PLAYING) {
                PuffButton(
                    onPuff = {
                        gameEngine.puff()
                        gameState = gameEngine.gameState
                    },
                    canPuff = gameEngine.balloon.canPuff,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                )
            }

            when (gameState.status) {
                GameStatus.READY -> {
                    StartOverlay(
                        onStart = {
                            gameEngine.startGame()
                            gameState = gameEngine.gameState
                        }
                    )
                }
                GameStatus.GAME_OVER -> {
                    GameOverOverlay(
                        score = gameState.score,
                        onRestart = {
                            gameEngine.resetGame()
                            gameEngine.startGame()
                            gameState = gameEngine.gameState
                        }
                    )
                }
                GameStatus.LEVEL_COMPLETE -> {
                    LevelCompleteOverlay(
                        score = gameState.score,
                        onRestart = {
                            gameEngine.resetGame()
                            gameEngine.startGame()
                            gameState = gameEngine.gameState
                        }
                    )
                }
                GameStatus.PLAYING -> {}
            }
        }
    }
}

@Composable
fun GameHUD(
    score: Int,
    air: Int,
    collectablesRemaining: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Score: $score",
            color = SpacePuffColors.UiText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Air: $air",
            color = if (air <= 3) SpacePuffColors.SharpRed else SpacePuffColors.UiText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Stars: $collectablesRemaining",
            color = SpacePuffColors.CollectableGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PuffButton(
    onPuff: () -> Unit,
    canPuff: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .background(
                color = if (canPuff) SpacePuffColors.PuffButton else SpacePuffColors.PuffButton.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onPuff()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PUFF",
            color = SpacePuffColors.UiText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StartOverlay(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onStart() })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SPACE PUFF",
                color = SpacePuffColors.BalloonBlue,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap PUFF to propel your balloon\nCollect all stars to win\nAvoid sharp obstacles!",
                color = SpacePuffColors.UiText,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Tap anywhere to start",
                color = SpacePuffColors.CollectableGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GameOverOverlay(score: Int, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAME OVER",
                color = SpacePuffColors.SharpRed,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Score: $score",
                color = SpacePuffColors.UiText,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpacePuffColors.PuffButton
                )
            ) {
                Text(
                    text = "Try Again",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LevelCompleteOverlay(score: Int, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LEVEL COMPLETE!",
                color = SpacePuffColors.CollectableGold,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Score: $score",
                color = SpacePuffColors.UiText,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpacePuffColors.PuffButton
                )
            ) {
                Text(
                    text = "Play Again",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
