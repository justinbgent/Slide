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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edgeline.slider.game.model.ChunkData
import com.edgeline.slider.game.model.Vector
import com.edgeline.slider.room.model.Score
import com.edgeline.slider.viewmodel.AppViewModelProvider
import com.edgeline.slider.viewmodel.GameViewModel
import kotlinx.coroutines.isActive

@Composable
fun Game(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = viewModel(factory = AppViewModelProvider.Factory)
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

    val canvasOffset by viewModel.canvasOffset.collectAsStateWithLifecycle()
    val visualData by viewModel.chunkData.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val playerDrawPoints by viewModel.playerPoints.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isGameOver.collectAsStateWithLifecycle()
    val scores by viewModel.highScores.collectAsStateWithLifecycle()

    Scaffold { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            TopBar(score, onNavigateBack)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            )
            {
                GameView(viewModel::onTap, canvasOffset, visualData, playerDrawPoints)
                if (isGameOver) {
                    GameOver(viewModel::restartGame, scores)
                }
            }
        }
    }
}

@Composable
fun TopBar(score: Int, onNavigateBack: () -> Unit) {
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
}

@Composable
fun GameView(
    onTap: (offset: Offset) -> Unit,
    canvasOffset: Vector,
    visualData: List<ChunkData>,
    playerDrawPoints: MutableList<Vector>
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color(0, 150, 191, 255))
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(onTap = onTap)
            }
    ) {
        translate(canvasOffset.x, canvasOffset.y) {
            for (data in visualData) {
                drawImage(data.bitmap, data.offset)
            }
            val player = Path().apply {
                if (playerDrawPoints.isNotEmpty()) {
                    moveTo(playerDrawPoints[0].x, playerDrawPoints[0].y)
                    playerDrawPoints.forEach { point ->
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
    }
}

@Composable
fun GameOver(restartGame: () -> Unit, scores: List<Score>) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .width(300.dp)
            .background(
                Color(0f, 0f, 0f, 0.6f),
                shape = RoundedCornerShape(corner = CornerSize(16.dp))
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Over",
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
        ShowHighScores(scores)
        Button(
            onClick = restartGame,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Restart")
        }
    }
}

@Composable
fun ShowHighScores(scores: List<Score>) {
    Text(
        text = "High Scores:",
        fontSize = 24.sp,
        color = Color.White,
        modifier = Modifier.padding(16.dp)
    )
    if (scores.size < 2) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            for (i in 0 until scores.size) {
                Text(
                    text = "${i + 1}. ${scores[i].score}",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            var greaterHalf = scores.size / 2
            if (scores.size % 2 != 0) greaterHalf++
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                for (i in 0 until greaterHalf) {
                    Text(
                        text = "${i + 1}. ${scores[i].score}",
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                for (i in greaterHalf until scores.size) {
                    Text(
                        text = "${i + 1}. ${scores[i].score}",
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
