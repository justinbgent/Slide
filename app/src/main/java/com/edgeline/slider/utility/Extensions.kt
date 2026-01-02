package com.edgeline.slider.utility

import androidx.compose.ui.geometry.Offset

fun Offset.addY(yChange: Float): Offset {
    return Offset(x, y + yChange)
}