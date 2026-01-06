package com.edgeline.slider.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector(var x: Float, var y: Float)

operator fun Vector.minus(other: Vector): Vector {
    return Vector(x - other.x, y - other.y)
}

operator fun Vector.times(num: Int): Vector {
    return Vector(x * num, y * num)
}
operator fun Vector.times(num: Float): Vector {
    return Vector(x * num, y * num)
}

operator fun Vector.plusAssign(other: Vector) {
    x += other.x
    y += other.y
}

fun Offset.toVector(): Vector {
    return Vector(x, y)
}

fun Vector.toOffset(): Offset {
    return Offset(x, y)
}

fun Vector.normalize(): Vector {
    val magnitude = sqrt(x * x + y * y)
    return Vector(x / magnitude, y / magnitude)
}

fun Vector.rotate(radians: Float): Vector {
    val cos = cos(radians)
    val sin = sin(radians)
    val newX = x * cos - y * sin
    val newY = x * sin + y * cos
    return Vector(newX, newY)
}

fun Vector.rotationTo(other: Vector): Float {
    return atan2(x * other.y - y * other.x, x * other.x + y * other.y)
}

// If is greater than 0 rotate CCW
// If is less than 0 rotate CW
// If is 0 rotate either direction
//fun Vector.crossProduct(other: Vector): Float {
//    return x * other.y - y * other.x
//}
