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
import com.edgeline.slider.model.Vector
import com.edgeline.slider.model.algorithm.Noise
import com.edgeline.slider.model.normalize
import com.edgeline.slider.model.rotate
import com.edgeline.slider.model.rotationTo
import com.edgeline.slider.model.times
import com.edgeline.slider.model.toOffset
import com.edgeline.slider.model.toVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.max

class GameViewModel() : ViewModel() {
    val playerSize = Size(32f, 32f)
    val playerCenter = Offset(playerSize.width / 2, playerSize.height / 2)
    val playerPosition: Offset
        get() = _playerPosition

    // Noise
    private val noise = Noise()
    private val baseSeed = 610495
    private val gameWidth = 4500
    private val chunkHeight = 2000
    private val halfChunkHeight = chunkHeight / 2
    private val goalDistance = 150f
    private val squareSize = 64f

    // Used to center chunk 0 on player
    private var chunkOffsetY = 0f

    //    val steadyGroundColor = Color(150, 111, 67, 255)
    private var screenHeight = 0
    private var screenWidth = 0
    private var coordinateOffset = Offset.Zero

    private var _playerPosition = Offset.Zero
    private var playerScreenPosition = Offset.Zero
    private var direction = Vector(0f, 1f)
    private val speed = 200f
    private val turnSpeed = PI.toFloat() / 3f
    private var goalDirection = Vector(0f, 0f)

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
        strokeWidth = 4f
    }

    fun setScreenSize(height: Int, width: Int) {
        screenHeight = height
        screenWidth = width
        chunkTopLeftX = -(gameWidth - screenWidth) / 2f
        chunksToLoad = max(screenHeight / chunkHeight + 1, 3)
        playerScreenPosition = Offset(screenWidth / 2f, screenHeight / 2f) - playerCenter
        _playerPosition = playerScreenPosition
        Log.i(GameViewModel::class.simpleName, "Chunks to load: $chunksToLoad")
        chunkOffsetY = playerScreenPosition.y - halfChunkHeight
    }

    fun setBitmapListener(listener: (data: List<ChunkData>) -> Unit) {
        bitmapListener = listener
    }

    fun updateGameState(deltaTime: Long): Offset {
        val timeStep = deltaTime / 1000f
        moveLogic(timeStep)
        _playerPosition = playerScreenPosition - coordinateOffset
        return coordinateOffset
    }

    fun tapVector(tapPosition: Offset) {
        goalDirection = (tapPosition - playerScreenPosition).toVector() * -1
    }

    private fun moveLogic(timeStep: Float) {
        val maxRotation = turnSpeed * timeStep
        val rot = Math.clamp(direction.rotationTo(goalDirection), -maxRotation, maxRotation)
        direction = direction.rotate(rot).normalize()
        coordinateOffset += (direction * speed * timeStep).toOffset()
    }

    suspend fun updateAndGetChunksIfNeeded(): List<ChunkData>? {
        val playerYTravel = -(playerPosition.y - playerScreenPosition.y).toInt()
        currentChunk = (halfChunkHeight + playerYTravel) / chunkHeight
        if (lastChunk != currentChunk) {
            val chunkMovement = currentChunk - lastChunk
            lastChunk = currentChunk

            return withContext(Dispatchers.Default) {
                updateChunkList(chunkMovement)
            }
        }
        return null
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
                // Span the same amount of units as squareSize
                point.x + squareSize - 1,
                point.y + squareSize - 1,
                paint
            )
        }
        val offset = Offset(chunkTopLeftX, (-chunk * chunkHeight + chunkOffsetY))
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
        // Plus subtract squareSize from right and bottom bounds to account for the squares
        val quarterGoal = goalDistance / 4
        val topLeft = Offset(0f, quarterGoal)
        val newHeight = chunkHeight - quarterGoal * 2 - squareSize
        val seed = baseSeed + chunk
        return noise.sampleRectangle(
            gameWidth.toFloat() - squareSize,
            newHeight,
            goalDistance,
            topLeft,
            seed
        )
    }
}
// So when I translate the canvas pausitively, I'm shifting the grid down.
// Thus, more of the grid's negatives are visible and I need to draw to those.