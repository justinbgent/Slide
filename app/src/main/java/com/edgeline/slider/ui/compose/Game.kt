package com.edgeline.slider.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.edgeline.slider.viewmodel.GameViewModel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun Game(
    onNavigateBack : () -> Unit,
    viewModel: GameViewModel = GameViewModel()
) {
    val rectSizePx = Size(16f, 16f)
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

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        drawRect(
            topLeft = Offset(500f, 550f),
            color = Color.Blue,
            size = rectSizePx
        )
    }
}

@Composable
fun MovableCircle(
    modifier: Modifier = Modifier,
    circleDiameter: Dp,
    speed: Dp,
) {
    val density = LocalDensity.current
    val circleRadiusPx = with(density) { circleDiameter.toPx() / 2f }
    val oneDimensionSizePx = with(density) { circleDiameter.toPx() }
    val speedPx = with(density) { speed.toPx() }

    var targetPosition by remember { mutableStateOf<Offset?>(null) }
    val animatedPosition = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (targetPosition == null) {
                        targetPosition = offset
                        scope.launch {
                            animatedPosition.snapTo(offset)
                        }
                    } else {
                        targetPosition = offset
                        scope.launch {
                            val currentPosition = animatedPosition.value
                            val distance = (offset - currentPosition).getDistance()
                            val durationMillis = (distance / speedPx * 1000).toInt()
                            animatedPosition.animateTo(
                                targetValue = offset,
                                animationSpec = tween(durationMillis = durationMillis)
                            )
                        }
                    }
                }
            }
    ) {
        targetPosition?.let {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Blue,
                    radius = circleRadiusPx,
                    center = animatedPosition.value
                )
            }
        }
    }
}