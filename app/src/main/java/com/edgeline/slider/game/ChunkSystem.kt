package com.edgeline.slider.game

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import com.edgeline.slider.model.ChunkData
import com.edgeline.slider.model.OffsetPoints
import com.edgeline.slider.model.algorithm.Noise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.div
import kotlin.math.max
import kotlin.unaryMinus

class ChunkSystem {
    companion object {
        const val SQUARE_SIZE = 64f
        const val MIN_CHUNK_LOAD = 3
    }

    val chunkWidth = 4500
    val chunkHeight = 2000
    private val halfChunkHeight = chunkHeight / 2
    // Used to center chunk 0 on player

    private val noise = Noise()
    private val baseSeed = 6104970
    private val goalDistance = 200f
    private val chunkPoints = mutableMapOf<Int, List<Offset>>()

    private val halfStroke = 2f // Needed because half of stroke is outside the square dimensions
    private val paint = Paint().apply {
        color = Color.Black
        style = PaintingStyle.Stroke
        strokeWidth = halfStroke * 2f
    }

    // Calculated to make the canvas start in the horizontal center of the generated field
    private var chunkTopLeftX = 0f
    private var chunkOffsetY = 0f
    private var lastChunk = -1
    private var currentChunk = 0
    private val chunks = mutableListOf<ChunkData>()
    private var chunksToLoad = MIN_CHUNK_LOAD

    fun initialize(screenWidth: Int, screenHeight: Int, chunksCenteredOn: Float){
        chunkTopLeftX = -(chunkWidth - screenWidth) / 2f
        chunkOffsetY = chunksCenteredOn - halfChunkHeight
        chunksToLoad = max(screenHeight / chunkHeight + 1, MIN_CHUNK_LOAD)
    }

    fun getChunkPoints(): OffsetPoints {
        val yOffset = currentChunk * chunkHeight + chunkOffsetY
        if (chunkPoints.containsKey(currentChunk)) {
            val chunkPoints = chunkPoints[currentChunk]!!
            return OffsetPoints(chunkPoints, Offset(chunkTopLeftX, yOffset))
        }
        return OffsetPoints(listOf(), Offset.Zero)
    }

    suspend fun updateAndGetChunksIfNeeded(playerYTravel: Int): List<ChunkData>? {
        currentChunk = if (playerYTravel < 0) {
            (playerYTravel - halfChunkHeight) / chunkHeight
        } else {
            (halfChunkHeight + playerYTravel) / chunkHeight
        }
        if (lastChunk != currentChunk) {
            Log.i(ChunkSystem::class.simpleName, "Current Chunk: $currentChunk")
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
//        Log.i(GameViewModel::class.simpleName, "MidIndex: $midIndex")
//        Log.i(GameViewModel::class.simpleName, "ChunkMovement: $chunkMovement")

        if (chunks.isEmpty()) {
            for (i in 0 until chunksToLoad) {
                chunks.add(generateChunk(i - midIndex))
            }
//            Log.i(GameViewModel::class.simpleName, "ChunkList Size: ${chunks.size}")
        } else {
            // Find out whether I moved forward or backward a chunk and remove the far out and add
            // a closer chunk.
            val previousChunk = currentChunk - chunkMovement
//            Log.i(GameViewModel::class.simpleName, "Previous Chunk: $previousChunk")
            val chunk: ChunkData?
            if (chunkMovement > 0) {
                chunk = chunks.find { it.chunk == previousChunk - midIndex }
                chunks.add(generateChunk(currentChunk + midIndex))
            } else {
                chunk = chunks.find { it.chunk == previousChunk + chunksToLoad / 2 }
                chunks.add(generateChunk(currentChunk - midIndex))
            }
            if (chunk != null) {
                chunks.remove(chunk)
                chunkPoints.remove(chunk.chunk)
            }
        }
        // Make a shallow copy to return to UI thread. Deep copy isn't needed because objects in
        // list aren't modified.
        return chunks.toList()
    }

    private fun generateChunk(chunk: Int): ChunkData {
        val bitmap = ImageBitmap(chunkWidth, chunkHeight, ImageBitmapConfig.Argb8888)
        val canvas = Canvas(bitmap)

        val points = sampleRectArea(chunk)
        val yOffset = chunk * chunkHeight + chunkOffsetY

        for (point in points) {
            canvas.drawRect(
                point.x,
                point.y,
                point.x + SQUARE_SIZE,
                point.y + SQUARE_SIZE,
                paint
            )
        }
        chunkPoints[chunk] = points

        val offset = Offset(chunkTopLeftX, yOffset)
        return ChunkData(chunk, bitmap, offset)
    }

    private fun sampleRectArea(chunk: Int): List<Offset> {
        // Take in both height bounds by quarterGoal to make tiling nearly seamless
        // Plus subtract squareSize from right and bottom bounds to account for the squares
        // Then account for square's stroke that goes over square bounds by half of the stroke
        //      on the left and right
        val quarterGoal = goalDistance / 4
        val topLeft = Offset(0f + halfStroke, quarterGoal)
        val newHeight = chunkHeight - quarterGoal * 2 - SQUARE_SIZE
        val seed = baseSeed + chunk
        return noise.sampleRectangle(
            chunkWidth.toFloat() - SQUARE_SIZE - halfStroke * 2,
            newHeight,
            goalDistance,
            topLeft,
            seed
        )
    }

}