package com.edgeline.slider.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edgeline.slider.model.ChunkData
import com.edgeline.slider.model.Vector
import com.edgeline.slider.viewmodel.GameViewModel
import kotlinx.coroutines.isActive

@Composable
fun Game(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = GameViewModel()
) {
//    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    // Runs once when composable enters the screen
    LaunchedEffect(Unit) {
        viewModel.initialize(windowInfo.containerSize.width, windowInfo.containerSize.height)
        while (isActive) {
            val now = withFrameMillis { it }
            viewModel.gameLoop(now)
        }
    }

    val bgColor = remember { Color(0, 183, 255, 255) }

    val canvasOffset by viewModel.canvasOffset.collectAsStateWithLifecycle()
    val visualData by viewModel.chunkData.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val playerPoints by viewModel.playerPoints.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isGameOver.collectAsStateWithLifecycle()

    Scaffold { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Score: $score", fontSize = 24.sp)
                Button(onClick = onNavigateBack) {
                    Text(text = "Return to Menu")
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            )
            {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(color = bgColor)
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                viewModel.tapVector(offset)
                            }
                        }
                ) {
                    drawVisuals(
                        canvasOffset,
                        visualData,
                        playerPoints
                    )
                }
                if (isGameOver) {
                    Column {
                        Text(
                            text = "Game Over",
                            fontSize = 24.sp,
                            color = Color.White
                        )
                        Button(onClick = viewModel::restartGame) {
                            Text(text = "Restart")
                        }
                    }
                }
            }
        }
    }
}

fun DrawScope.drawVisuals(
    canvasOffset: Vector,
    visualData: List<ChunkData>,
    points: MutableList<Vector>
) {
    translate(canvasOffset.x, canvasOffset.y) {
        for (data in visualData) {
            drawImage(data.bitmap, data.offset)
        }
        val player = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (point in points) {
                    lineTo(point.x, point.y)
                }
                close()
            }
        }
        drawPath(
            path = player,
            color = Color.Blue,
            style = Fill
        )
    }
//    drawCircle(
//        color = Color.Gray,
//        radius = 32f,
//        center = Offset(100f, 100f)
//    )
}

// Directional line
//                    val playerCenter = Offset(viewModel.playerPosition.x + viewModel.playerCenter.x, viewModel.playerPosition.y + viewModel.playerCenter.y)
//                    val lineDirection = viewModel.direction * 48f
//                    val lineEnd = Offset(playerCenter.x + lineDirection.x, playerCenter.y + lineDirection.y)
