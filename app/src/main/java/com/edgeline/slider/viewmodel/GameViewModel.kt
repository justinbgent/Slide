package com.edgeline.slider.viewmodel

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.lifecycle.ViewModel
import com.edgeline.slider.model.algorithm.BlueNoise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class GameViewModel : ViewModel() {
    val logicScope = CoroutineScope(Dispatchers.Default)
    val noise = BlueNoise()

    private val paint = Paint().apply {
        color = Color.Red
        style = PaintingStyle.Fill
    }

    // TODO: Bitmaps of 4500x1500 pixels
    fun getBitmap(): ImageBitmap {
        return getBitmap(4500, 1500, 0f, 0f)
    }

    fun getBitmap(width: Int, height: Int, xOffset: Float, yOffset: Float): ImageBitmap {
        val points = noise.sampleRectangle(width, height, 100f, xOffset, yOffset)
        val bitmap = ImageBitmap(width, height, ImageBitmapConfig.Argb8888)

        val canvas = Canvas(bitmap)

        val squareSize = 64f

        for (point in points) {
            canvas.drawRect(
                point.x,
                point.y,
                point.x + squareSize,
                point.y + squareSize,
                paint)
        }

        return bitmap
    }
}