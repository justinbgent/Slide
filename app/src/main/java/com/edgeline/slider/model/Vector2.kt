package com.edgeline.slider.model

data class Vector2(var x: Float, var y: Float)

operator fun Vector2.minus(other: Vector2): Vector2 {
    return Vector2(x - other.x, y - other.y)
}

operator fun Vector2.times(num: Int): Vector2 {
    return Vector2(x * num, y * num)
}

operator fun Vector2.plusAssign(other: Vector2) {
    x += other.x
    y += other.y
}