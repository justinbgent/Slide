package com.edgeline.slider.model

class Rectangle(x: Float, y: Float, width: Float, height: Float) {
    val top = y
    val bottom = y + height
    val left = x
    val right = x + width

    fun Intersects(rect: Rectangle): Boolean {
        // If one is above or below the other
        if (top >= rect.bottom || bottom <= rect.top) {
            return false
        }
        // If one is to the left or right of the other
        if (left >= rect.right || right <= rect.left) {
            return false
        }

        return true
    }
}