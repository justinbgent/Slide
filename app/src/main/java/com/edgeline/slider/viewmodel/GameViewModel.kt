package com.edgeline.slider.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.lifecycle.ViewModel
import com.edgeline.slider.model.ChunkData
import com.edgeline.slider.model.algorithm.Noise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class GameViewModel() : ViewModel() {
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
    private var topLeftX = 0f
    private var yOffset = 0f
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
        topLeftX = -(gameWidth - screenWidth) / 2f
        chunksToLoad = max(screenHeight / chunkHeight + 1, 3)
    }
    fun generateBitmap(listener: (data: List<ChunkData>) -> Unit) {
        bitmapListener = listener
        getNewBitmap(0)
    }

    fun updateCanvasYOffset(canvasOffsetY: Float){
        yOffset = canvasOffsetY
        currentChunk = yOffset.toInt() / chunkHeight
        if (lastChunk != currentChunk) {
            getNewBitmap(currentChunk - lastChunk)
            lastChunk = currentChunk
        }
    }

    // For collision checks
    fun updatePlayerPosition(newPosition: Offset){
    }

    private fun getNewBitmap(chunkMovement: Int) {
        logicScope.launch {
            val data = getBitmaps(chunkMovement)
            mainScope.launch {
                bitmapListener(data)
            }
        }
    }

    private fun getBitmaps(chunkMovement: Int): List<ChunkData> {
        val midIndex = if (chunksToLoad % 2 != 0){ chunksToLoad / 2 }
        else { chunksToLoad / 2 - 1 }

        if (chunks.size != chunksToLoad){
            for (i in 0 until chunksToLoad){
                chunks.add(getBitmap(i - midIndex))
            }
        }
        else {
            // Find out whether I moved forward or backward a chunk and remove the far out and add
            // a closer chunk.
            val previousChunk = currentChunk + chunkMovement
            if (chunkMovement > 0){
                val chunk = chunks.find { it.chunk == previousChunk - midIndex }
                chunks.remove(chunk)
                chunks.add(getBitmap(currentChunk + midIndex))
            } else {
                val chunk = chunks.find { it.chunk == previousChunk + chunksToLoad / 2 }
                chunks.remove(chunk)
                chunks.add(getBitmap(currentChunk - midIndex))
            }
        }
        return chunks
    }

    private fun getBitmap(chunk: Int): ChunkData {
        val bitmap = ImageBitmap(gameWidth, chunkHeight, ImageBitmapConfig.Argb8888)
        val canvas = Canvas(bitmap)

        val points = sampleRect(chunk)

        for (point in points) {
            canvas.drawRect(
                point.x,
                point.y,
                point.x + squareSize,
                point.y + squareSize,
                paint)
        }

        return ChunkData(chunk, bitmap, Offset(topLeftX, -chunk * chunkHeight.toFloat()))
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