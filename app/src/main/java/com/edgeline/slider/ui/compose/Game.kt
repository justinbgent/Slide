package com.edgeline.slider.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import com.edgeline.slider.model.ChunkData
import com.edgeline.slider.utility.addY
import com.edgeline.slider.viewmodel.GameViewModel
import kotlinx.coroutines.isActive

@Composable
fun Game(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = GameViewModel()
) {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    // remember like this maintains whatever value it is set to over recompositions
    // but it does not trigger a recomposition.
    val rectSizePx = remember { Size(32f, 32f) }
    val rectCenter = remember { Offset(rectSizePx.width / 2, rectSizePx.height / 2) }
    val bgColor = remember { Color(0, 183, 255, 255) }
    var frameTimestamp = remember { 0L }

    // remember with mutableStateOf makes it observable and changing it will trigger
    // a recomposition.
    var coordinateOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var chunkData by remember { mutableStateOf(listOf<ChunkData>()) }

    // Runs once when composable enters the screen
    LaunchedEffect(Unit) {
        viewModel.setScreenSize(windowInfo.containerSize.height, windowInfo.containerSize.width)
        viewModel.generateBitmap { data ->
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

            val coordinateChange = 100f * (now - frameTimestamp) / 1000f
            // Update state which triggers recomposition for next frame
            coordinateOffset = coordinateOffset.addY(coordinateChange)
            viewModel.updateCanvasYOffset(coordinateOffset.y)

            frameTimestamp = now
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
        ) {
            translate(coordinateOffset.x, coordinateOffset.y) {
                for (data in chunkData) {
                    drawImage(data.bitmap, data.offset)
                }
                val playerPosition = this@Canvas.center - coordinateOffset - rectCenter
                viewModel.updatePlayerPosition(playerPosition)
                drawRect(
                    topLeft = playerPosition,
                    color = Color.Blue,
                    size = rectSizePx
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun GamePreview() {
//    Game(onNavigateBack = {})
//}

//

//@Composable
//fun MovableCircle(
//    modifier: Modifier = Modifier,
//    circleDiameter: Dp,
//    speed: Dp,
//) {
//    val density = LocalDensity.current
//    val circleRadiusPx = with(density) { circleDiameter.toPx() / 2f }
//    val oneDimensionSizePx = with(density) { circleDiameter.toPx() }
//    val speedPx = with(density) { speed.toPx() }
//
//    var targetPosition by remember { mutableStateOf<Offset?>(null) }
//    val animatedPosition = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
//
//    val scope = rememberCoroutineScope()
//
//    Box(
//        modifier = modifier
//            .pointerInput(Unit) {
//                detectTapGestures { offset ->
//                    if (targetPosition == null) {
//                        targetPosition = offset
//                        scope.launch {
//                            animatedPosition.snapTo(offset)
//                        }
//                    } else {
//                        targetPosition = offset
//                        scope.launch {
//                            val currentPosition = animatedPosition.value
//                            val distance = (offset - currentPosition).getDistance()
//                            val durationMillis = (distance / speedPx * 1000).toInt()
//                            animatedPosition.animateTo(
//                                targetValue = offset,
//                                animationSpec = tween(durationMillis = durationMillis)
//                            )
//                        }
//                    }
//                }
//            }
//    ) {
//        targetPosition?.let {
//            Canvas(modifier = Modifier.fillMaxSize()) {
//                drawCircle(
//                    color = Color.Blue,
//                    radius = circleRadiusPx,
//                    center = animatedPosition.value
//                )
//            }
//        }
//    }
//}
