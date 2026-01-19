package com.spacepuff.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.spacepuff.entities.Balloon
import com.spacepuff.entities.Collectable
import com.spacepuff.entities.Obstacle
import com.spacepuff.entities.ObstacleType
import com.spacepuff.ui.theme.SpacePuffColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvas(
    balloon: Balloon,
    obstacles: List<Obstacle>,
    collectables: List<Collectable>,
    cameraX: Float,
    cameraY: Float,
    modifier: Modifier = Modifier
) {
    val stars = remember { generateStars(300) }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawStarfield(stars, cameraX, cameraY)

        obstacles.forEach { obstacle ->
            if (obstacle.isActive) {
                drawObstacle(obstacle, cameraX, cameraY)
            }
        }

        collectables.forEach { collectable ->
            if (collectable.isActive) {
                drawCollectable(collectable, cameraX, cameraY)
            }
        }

        if (balloon.isActive) {
            drawBalloon(balloon, cameraX, cameraY)
        }
    }
}

private fun generateStars(count: Int): List<Pair<Float, Float>> {
    return List(count) {
        Random.nextFloat() to Random.nextFloat()
    }
}

private fun DrawScope.drawStarfield(stars: List<Pair<Float, Float>>, cameraX: Float, cameraY: Float) {
    // Parallax effect - stars move slower than camera
    val parallax = 0.1f
    stars.forEach { (xRatio, yRatio) ->
        val x = (xRatio * size.width * 3f - cameraX * parallax) % size.width
        val y = (yRatio * size.height * 3f - cameraY * parallax) % size.height
        drawCircle(
            color = SpacePuffColors.StarField,
            radius = 1.5f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawBalloon(balloon: Balloon, cameraX: Float, cameraY: Float) {
    val center = Offset(balloon.position.x - cameraX, balloon.position.y - cameraY)

    drawCircle(
        color = SpacePuffColors.BalloonBlue,
        radius = balloon.radius,
        center = center
    )

    drawCircle(
        color = SpacePuffColors.BalloonHighlight.copy(alpha = 0.3f),
        radius = balloon.radius * 0.4f,
        center = Offset(
            center.x - balloon.radius * 0.3f,
            center.y - balloon.radius * 0.3f
        )
    )

    rotate(
        degrees = Math.toDegrees(balloon.rotation.toDouble()).toFloat(),
        pivot = center
    ) {
        val nozzleStart = Offset(center.x + balloon.radius * 0.8f, center.y)
        val nozzleEnd = Offset(center.x + balloon.radius * 1.3f, center.y)

        drawLine(
            color = SpacePuffColors.BalloonBlue.copy(alpha = 0.8f),
            start = nozzleStart,
            end = nozzleEnd,
            strokeWidth = 8f
        )

        drawCircle(
            color = Color.DarkGray,
            radius = 6f,
            center = nozzleEnd
        )
    }

    drawCircle(
        color = SpacePuffColors.BalloonBlue.copy(alpha = 0.5f),
        radius = balloon.radius + 3f,
        center = center,
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawObstacle(obstacle: Obstacle, cameraX: Float, cameraY: Float) {
    val center = Offset(obstacle.position.x - cameraX, obstacle.position.y - cameraY)

    when (obstacle.type) {
        ObstacleType.SHARP -> drawSharpObstacle(center, obstacle.radius, obstacle.spikeCount, obstacle.rotation)
        ObstacleType.NEUTRAL -> drawNeutralObstacle(center, obstacle.radius, obstacle.rotation)
    }
}

private fun DrawScope.drawSharpObstacle(
    center: Offset,
    radius: Float,
    spikeCount: Int,
    rotation: Float
) {
    rotate(
        degrees = Math.toDegrees(rotation.toDouble()).toFloat(),
        pivot = center
    ) {
        drawCircle(
            color = SpacePuffColors.SharpRed.copy(alpha = 0.7f),
            radius = radius * 0.7f,
            center = center
        )

        val path = Path()
        val angleStep = (2 * Math.PI / spikeCount).toFloat()

        for (i in 0 until spikeCount) {
            val angle = i * angleStep
            val innerRadius = radius * 0.6f
            val outerRadius = radius * 1.2f

            val innerX = center.x + cos(angle) * innerRadius
            val innerY = center.y + sin(angle) * innerRadius
            val outerX = center.x + cos(angle) * outerRadius
            val outerY = center.y + sin(angle) * outerRadius

            if (i == 0) {
                path.moveTo(innerX, innerY)
            }

            path.lineTo(outerX, outerY)

            val nextAngle = (i + 0.5f) * angleStep
            val nextInnerX = center.x + cos(nextAngle) * innerRadius
            val nextInnerY = center.y + sin(nextAngle) * innerRadius
            path.lineTo(nextInnerX, nextInnerY)
        }
        path.close()

        drawPath(
            path = path,
            color = SpacePuffColors.SharpRed,
            style = Fill
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = radius * 0.2f,
            center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f)
        )
    }
}

private fun DrawScope.drawNeutralObstacle(center: Offset, radius: Float, rotation: Float) {
    rotate(
        degrees = Math.toDegrees(rotation.toDouble()).toFloat(),
        pivot = center
    ) {
        drawCircle(
            color = SpacePuffColors.NeutralGray,
            radius = radius,
            center = center
        )

        for (i in 0..2) {
            val craterOffset = Offset(
                center.x + cos(i * 2.1f) * radius * 0.4f,
                center.y + sin(i * 2.1f) * radius * 0.4f
            )
            drawCircle(
                color = SpacePuffColors.NeutralGray.copy(alpha = 0.5f),
                radius = radius * 0.15f,
                center = craterOffset
            )
        }

        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = radius * 0.25f,
            center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f)
        )

        drawCircle(
            color = SpacePuffColors.NeutralGray.copy(alpha = 0.7f),
            radius = radius + 2f,
            center = center,
            style = Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawCollectable(collectable: Collectable, cameraX: Float, cameraY: Float) {
    val center = Offset(collectable.position.x - cameraX, collectable.position.y - cameraY)
    val scaledRadius = collectable.radius * collectable.pulseScale

    drawCircle(
        color = SpacePuffColors.CollectableGlow,
        radius = scaledRadius * 1.5f,
        center = center
    )

    val path = Path()
    val points = 5
    val outerRadius = scaledRadius
    val innerRadius = scaledRadius * 0.4f

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * Math.PI / points - Math.PI / 2).toFloat()
        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    drawPath(
        path = path,
        color = SpacePuffColors.CollectableGold,
        style = Fill
    )

    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.5f),
        style = Stroke(width = 2f)
    )
}
