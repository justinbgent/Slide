package com.edgeline.slider.game.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector(var x: Float, var y: Float)

operator fun Vector.minus(other: Vector): Vector {
    return Vector(x - other.x, y - other.y)
}

operator fun Vector.plus(other: Vector): Vector {
    return Vector(x + other.x, y + other.y)
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
fun Vector.set(other: Vector) {
    x = other.x
    y = other.y
}
fun Vector.set(newX: Float, newY: Float) {
    x = newX
    y = newY
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

// Clockwise Perpendicular
fun Vector.cwPerpendicular(): Vector {
    return Vector(-y, x)
}

// Counter Clockwise Perpendicular
fun Vector.ccwPerpendicular(): Vector {
    return Vector(y, -x)
}

// Rotates Clockwise 90
fun Vector.rotateCw90() {
    val temp = x
    x = -y
    y = temp
}

// Rotates Counter Clockwise 90
fun Vector.rotateCcw90() {
    val temp = x
    x = y
    y = -temp
}

// If is greater than 0 rotate CCW
// If is less than 0 rotate CW
// If is 0 rotate either direction
//fun Vector.crossProduct(other: Vector): Float {
//    return x * other.y - y * other.x
//}
