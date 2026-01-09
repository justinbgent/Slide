package com.edgeline.slider.model

import kotlin.math.max
import kotlin.math.min

class Circle(val center: Vector, val radius: Float) {
    val radiusSquared = radius * radius

    fun contains(point: Vector): Boolean {
        val distanceVector = center - point
        val sqrDistance = distanceVector.x * distanceVector.x + distanceVector.y * distanceVector.y
        return sqrDistance <= radiusSquared
    }

    fun intersects(circle: Circle): Boolean {
        val distanceVector = center - circle.center
        val sqrDistance = distanceVector.x * distanceVector.x + distanceVector.y * distanceVector.y
        val radiusSum = radius + circle.radius
        return sqrDistance <= radiusSum * radiusSum
    }

    fun intersects(rect: Rectangle): Boolean {
        val closestVector = Vector(
            max(rect.left, min(center.x, rect.right)),
            max(rect.top, min(center.y, rect.bottom))
        )
        return contains(closestVector)
    }

}