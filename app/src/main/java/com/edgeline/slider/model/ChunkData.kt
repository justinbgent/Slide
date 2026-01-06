package com.edgeline.slider.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

data class ChunkData(
    val chunk: Int,
    val bitmap: ImageBitmap,
    val offset: Offset
)
