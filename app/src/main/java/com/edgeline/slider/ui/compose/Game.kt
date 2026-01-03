package com.edgeline.slider.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgeline.slider.model.ChunkData
import com.edgeline.slider.viewmodel.GameViewModel
import kotlinx.coroutines.isActive

@Composable
fun Game(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = GameViewModel()
) {
//    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    // remember like this maintains whatever value it is set to over recompositions
    // but it does not trigger a recomposition.
    val bgColor = remember { Color(0, 183, 255, 255) }
    var collisionRect = remember { Offset(0f, 0f) }

    // remember with mutableStateOf makes it observable and changing it will trigger
    // a recomposition.
    var canvasOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var chunkData by remember { mutableStateOf(listOf<ChunkData>()) }
    var score by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    // Runs once when composable enters the screen
    LaunchedEffect(Unit) {
        viewModel.setScreenSize(windowInfo.containerSize.height, windowInfo.containerSize.width)
        // Doesn't need remember because LaunchedEffect with key Unit survives across recompositions
        var frameTimestamp = 0L
        // Loop indefinitely while composable is on screen
        while (isActive) {
            // Suspends till the next animation frame and gets timestamp
            val now = withFrameMillis { it }

            // Don't calculate movement on the first frame
            if (frameTimestamp == 0L) {
                frameTimestamp = now
                continue
            }

            // Update state which triggers recomposition for next frame
            canvasOffset = viewModel.updateGameState(now - frameTimestamp)

            // Update chunks
            val newChunks = viewModel.updateAndGetChunksIfNeeded()
            if (newChunks != null) {
                chunkData = newChunks
            }

            if (viewModel.isGameOver()){
                isGameOver = true
                collisionRect = viewModel.endRectPos
                break
            }

            // Update score
            if (viewModel.score != score){
                score = viewModel.score
            }

            frameTimestamp = now
        }
    }

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
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(color = bgColor)
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            viewModel.tapVector(offset)
                        }
                    }
            ) {
                translate(canvasOffset.x, canvasOffset.y) {
                    for (data in chunkData) {
                        drawImage(data.bitmap, data.offset)
                    }

                    // Directional line
                    val playerCenter = Offset(viewModel.playerPosition.x + viewModel.playerCenter.x, viewModel.playerPosition.y + viewModel.playerCenter.y)
                    val lineEnd = Offset(playerCenter.x - viewModel.direction.x * 48f, playerCenter.y - viewModel.direction.y * 48f)
                    val arrowPath = Path().apply {
                        moveTo(playerCenter.x, playerCenter.y)
                        lineTo(lineEnd.x, lineEnd.y)
                    }
                    drawPath(
                        path = arrowPath,
                        color = Color.Black,
                        style = Stroke(8f, cap = StrokeCap.Round)
                    )

                    // Player
                    drawRect(
                        topLeft = viewModel.playerPosition,
                        color = Color.Blue,
                        size = viewModel.playerSize
                    )

                    // Collision
                    if (isGameOver) {
                        drawRect(
                            topLeft = collisionRect,
                            color = Color.Red,
                            size = Size(64f, 64f)
                        )
                    }
                }
            }
        }
    }
}
