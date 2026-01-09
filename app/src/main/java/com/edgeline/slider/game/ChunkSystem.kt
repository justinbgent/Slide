package com.edgeline.slider.game

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendModeColorFilter
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.math.max

class ChunkSystem {
    companion object {
        const val SQUARE_SIZE = 64f
        const val BOUNDARY_WIDTH = SQUARE_SIZE * 2
        const val SAMPLE_WIDTH = 4500
        const val CHUNK_HEIGHT = 2000
        const val CANVAS_WIDTH = (BOUNDARY_WIDTH * 2).toInt() + SAMPLE_WIDTH
        val boundary1Coord = Offset(0f, 0f)
        val boundary2Coord = Offset(BOUNDARY_WIDTH + SAMPLE_WIDTH, 0f)
        private const val MIN_CHUNK_LOAD = 3
    }

    private var chunkJob: Job? = null

    private val halfChunkHeight = CHUNK_HEIGHT / 2
    // Used to center chunk 0 on player

    private val paint = Paint().apply {
        color = Color(0, 61, 128, 255)
        style = PaintingStyle.Fill
    }

    private val noise = Noise()
    private val baseSeed = 6104970
    private val goalDistance = 200f
    // Calculated to make the canvas start in the horizontal center of the generated field
    private var sampleTopLeftX = 0f
    private var chunkStartY = 0f
    private var lastChunk = -1
    private var currentChunk = 0
    private val chunks = mutableListOf<ChunkData>()
    private val chunkPoints = mutableMapOf<Int, List<Offset>>()
    private var chunksToLoad = MIN_CHUNK_LOAD

    fun restart(){
        chunks.clear()
        chunkPoints.clear()
        currentChunk = 0
        lastChunk = -1
    }

    fun initialize(screenWidth: Int, screenHeight: Int, chunksCenteredOn: Float){
        sampleTopLeftX = -(SAMPLE_WIDTH - screenWidth) / 2f
        chunkStartY = chunksCenteredOn - halfChunkHeight
        chunksToLoad = max(screenHeight / CHUNK_HEIGHT + 1, MIN_CHUNK_LOAD)
    }

    fun getChunkPoints(): OffsetPoints {
        if (chunkPoints.containsKey(currentChunk)) {
            val chunkPoints = chunkPoints[currentChunk]!!
            val yOffset = currentChunk * CHUNK_HEIGHT + chunkStartY
            return OffsetPoints(chunkPoints, Offset(sampleTopLeftX, yOffset))
        }
        return OffsetPoints(listOf(), Offset.Zero)
    }

    suspend fun updateAndGetChunksIfNeeded(playerYTravel: Int): List<ChunkData>? {
        currentChunk = if (playerYTravel < 0) {
            (playerYTravel - halfChunkHeight) / CHUNK_HEIGHT
        } else {
            (halfChunkHeight + playerYTravel) / CHUNK_HEIGHT
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
        val bitmap = ImageBitmap(CANVAS_WIDTH, CHUNK_HEIGHT, ImageBitmapConfig.Argb8888)
        val canvas = Canvas(bitmap)

        val points = sampleRectArea(chunk)
        val yOffset = chunk * CHUNK_HEIGHT + chunkStartY

        canvas.drawRect(
            boundary1Coord.x,
            boundary1Coord.y,
            BOUNDARY_WIDTH,
            CHUNK_HEIGHT.toFloat(),
            paint
        )
        canvas.drawRect(
            boundary2Coord.x,
            boundary2Coord.y,
            CANVAS_WIDTH.toFloat(),
            CHUNK_HEIGHT.toFloat(),
            paint
        )
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

        val offset = Offset(sampleTopLeftX, yOffset)
        return ChunkData(chunk, bitmap, offset)
    }

    private fun sampleRectArea(chunk: Int): List<Offset> {
        // Take in both height bounds by quarterGoal to make tiling nearly seamless
        // Plus subtract squareSize from right and bottom bounds to account for the squares
        val quarterGoal = goalDistance / 4
        val topLeft = Offset(BOUNDARY_WIDTH, quarterGoal)
        val newHeight = CHUNK_HEIGHT - quarterGoal * 2 - SQUARE_SIZE
        val seed = baseSeed + chunk
        return noise.sampleRectangle(
            SAMPLE_WIDTH.toFloat() - SQUARE_SIZE,
            newHeight,
            goalDistance,
            topLeft,
            seed
        )
    }

}