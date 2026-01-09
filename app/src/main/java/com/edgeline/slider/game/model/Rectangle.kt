package com.edgeline.slider.game.model

import kotlin.math.max
import kotlin.math.min

class Rectangle(x: Float, y: Float, width: Float, height: Float) {
    val top = y
    val bottom = y + height
    val left = x
    val right = x + width

    fun contains(point: Vector): Boolean {
        return point.x in left..right &&
                point.y in top .. bottom
    }

    fun intersects(rect: Rectangle): Boolean {
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

    fun intersects(circle: Circle): Boolean {
        val closestVector = Vector(
            max(left, min(circle.center.x, right)),
            max(top, min(circle.center.y, bottom))
        )
        return circle.contains(closestVector)
    }

}