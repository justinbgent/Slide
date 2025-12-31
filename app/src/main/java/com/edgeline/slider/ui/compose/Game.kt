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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edgeline.slider.viewmodel.GameViewModel
import kotlinx.coroutines.isActive

@Composable
fun Game(
    onNavigateBack : () -> Unit,
    viewModel: GameViewModel = GameViewModel()
) {
    val localDensity = LocalDensity.current
    // remember like this maintains whatever value it is set to over recompositions
    // but it does not trigger a recomposition.
    val rectSizePx = remember { Size(32f, 32f) }
    val rectCenter = remember { Offset(rectSizePx.width / 2, rectSizePx.height / 2) }
    var canvasOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    // remember with mutableStateOf makes it observable and changing it will trigger
    // a recomposition.
    var frameTimestamp by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        // Loop indefinitely while composable is on screen
        while (isActive) {
            // Suspends till the next animation frame and gets timestamp
            val now = withFrameMillis { it }
            // Update state which triggers recomposition for next frame
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
                .background(color = Color(0, 183, 255, 255))
        ) {
            translate(canvasOffset.x, canvasOffset.y) {
                // Todo: Don't get a bitmap every recomposition, only do it when a new chunk is needed.
                drawImage(viewModel.getBitmap(), Offset(0f, 0f))
                drawRect(
                    topLeft = this@Canvas.center - rectCenter,
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
