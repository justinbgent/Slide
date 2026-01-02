package com.edgeline.slider.viewmodel

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.lifecycle.ViewModel
import com.edgeline.slider.model.ChunkData
import com.edgeline.slider.model.algorithm.Noise
import com.edgeline.slider.utility.addY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class GameViewModel() : ViewModel() {
    val playerSize = Size(32f, 32f)
    val playerCenter = Offset(playerSize.width / 2, playerSize.height / 2)
    val playerPosition: Offset
        get() = _playerPosition
    private var _playerPosition = Offset.Zero
    private var playerScreenPosition = Offset.Zero
    private var coordinateOffset = Offset.Zero
    private val logicScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val noise = Noise()
    private val baseSeed = 6104978
    private val gameWidth = 4500
    private val chunkHeight = 2000
    private val goalDistance = 200f
    private val squareSize = 64f

    //    val steadyGroundColor = Color(150, 111, 67, 255)
    private var screenHeight = 0
    private var screenWidth = 0

    // Calculated to make the canvas start in the horizontal center of the generated field
    private var chunkTopLeftX = 0f
    private var lastChunk = -1
    private var currentChunk = 0
    private lateinit var bitmapListener: (data: List<ChunkData>) -> Unit
    private val chunks = mutableListOf<ChunkData>()
    private var chunksToLoad: Int = 0

    private val paint = Paint().apply {
        color = Color.Black
        style = PaintingStyle.Stroke
        strokeWidth = 2f
    }

    fun setScreenSize(height: Int, width: Int) {
        screenHeight = height
        screenWidth = width
        chunkTopLeftX = -(gameWidth - screenWidth) / 2f
        chunksToLoad = max(screenHeight / chunkHeight + 1, 3)
        playerScreenPosition = Offset(screenWidth / 2f, screenHeight / 2f) - playerCenter
        _playerPosition = playerScreenPosition
        Log.i(GameViewModel::class.simpleName, "Chunks to load: $chunksToLoad")
    }

    fun setBitmapListener(listener: (data: List<ChunkData>) -> Unit) {
        bitmapListener = listener
    }

    fun getCoordinateOffset(deltaTime: Long): Offset {
        val timeStep = deltaTime / 1000f
        val coordinateChange = 100f * timeStep
        coordinateOffset = coordinateOffset.addY(coordinateChange)
        _playerPosition = playerScreenPosition - coordinateOffset
        updateChunkData()
        return coordinateOffset
    }

    private fun updateChunkData() {
        currentChunk = coordinateOffset.y.toInt() / chunkHeight
        if (lastChunk != currentChunk) {
            getChunks(currentChunk - lastChunk)
            lastChunk = currentChunk
        }
    }

    private fun getChunks(chunkMovement: Int) {
        logicScope.launch {
            val data = updateChunkList(chunkMovement)
            mainScope.launch {
                bitmapListener(data)
            }
        }
    }

    private fun updateChunkList(chunkMovement: Int): List<ChunkData> {
        val midIndex = if (chunksToLoad % 2 != 0) {
            chunksToLoad / 2
        } else {
            chunksToLoad / 2 - 1
        }
        Log.i(GameViewModel::class.simpleName, "MidIndex: $midIndex")
        Log.i(GameViewModel::class.simpleName, "ChunkMovement: $chunkMovement")

        if (chunks.isEmpty()) {
            for (i in 0 until chunksToLoad) {
                chunks.add(generateChunk(i - midIndex))
            }
            Log.i(GameViewModel::class.simpleName, "ChunkList Size: ${chunks.size}")
        } else {
            // Find out whether I moved forward or backward a chunk and remove the far out and add
            // a closer chunk.
            val previousChunk = currentChunk - chunkMovement
            Log.i(GameViewModel::class.simpleName, "Previous Chunk: $previousChunk")
            if (chunkMovement > 0) {
                val chunk = chunks.find { it.chunk == previousChunk - midIndex }
                chunks.remove(chunk)
                chunks.add(generateChunk(currentChunk + midIndex))
            } else {
                val chunk = chunks.find { it.chunk == previousChunk + chunksToLoad / 2 }
                chunks.remove(chunk)
                chunks.add(generateChunk(currentChunk - midIndex))
            }
        }
        // Make a shallow copy to return to UI thread. Deep copy isn't needed because objects in
        // list aren't modified.
        return chunks.toList()
    }

    private fun generateChunk(chunk: Int): ChunkData {
        val bitmap = ImageBitmap(gameWidth, chunkHeight, ImageBitmapConfig.Argb8888)
        val canvas = Canvas(bitmap)

        val points = sampleRect(chunk)

        for (point in points) {
            canvas.drawRect(
                point.x,
                point.y,
                point.x + squareSize,
                point.y + squareSize,
                paint
            )
        }
        val offset = Offset(chunkTopLeftX, (-chunk * chunkHeight).toFloat())
        return ChunkData(chunk, bitmap, offset)
    }

    // I need to sample a rectangle for every chunk
    // Different screens will vary in the quantity of chunks that are shown
    // So somehow this viewmodel needs to calculate when a new chunk bitmap needs
    //      to be generated and when to no longer hold reference to old chunks.
    // My Game.kt compose file will need to simply draw all of the bitmaps it is given to
    //      the canvas.
    // So make the returned bitmap into a list of them.
    // Game.kt will need to feed me frame by frame updates of its center canvas coordinate
    //      from which I'll calculate all of this stuff.

    fun sampleRect(chunk: Int): List<Offset> {
        // Take in both height bounds by quarterGoal to make tiling nearly seamless
        val quarterGoal = goalDistance / 4
        val topLeft = Offset(0f, quarterGoal)
        val newHeight = chunkHeight - quarterGoal * 2
        val seed = baseSeed + chunk
        return noise.sampleRectangle(gameWidth.toFloat(), newHeight, goalDistance, topLeft, seed)
    }
}
// So when I translate the canvas pausitively, I'm shifting the grid down.
// Thus, more of the grid's negatives are visible and I need to draw to those.