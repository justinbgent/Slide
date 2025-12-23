package com.edgeline.slider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.edgeline.slider.ui.theme.SliderTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SliderTheme {
                MovableCircle(
                    modifier = Modifier.fillMaxSize(),
                    circleDiameter = 16.dp,
                    speed = 128.dp
                )
            }
        }
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