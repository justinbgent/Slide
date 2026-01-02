package com.edgeline.slider.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalWindowInfo
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
    var frameTimestamp = remember { 0L }

    // remember with mutableStateOf makes it observable and changing it will trigger
    // a recomposition.
    var canvasOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var chunkData by remember { mutableStateOf(listOf<ChunkData>()) }

    // Runs once when composable enters the screen
    LaunchedEffect(Unit) {
        viewModel.setScreenSize(windowInfo.containerSize.height, windowInfo.containerSize.width)
        viewModel.setBitmapListener { data ->
            chunkData = data
        }
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
            canvasOffset = viewModel.getCoordinateOffset(now - frameTimestamp)

            frameTimestamp = now
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.End
            ) {
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
            ) {
                translate(canvasOffset.x, canvasOffset.y) {
                    for (data in chunkData) {
                        drawImage(data.bitmap, data.offset)
                    }
                    drawRect(
                        topLeft = viewModel.playerPosition,
                        color = Color.Blue,
                        size = viewModel.playerSize
                    )
                }
            }
        }
    }
}
