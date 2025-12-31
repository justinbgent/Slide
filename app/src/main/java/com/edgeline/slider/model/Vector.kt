package com.edgeline.slider.model

data class Vector(var x: Float, var y: Float)

operator fun Vector.minus(other: Vector): Vector {
    return Vector(x - other.x, y - other.y)
}

operator fun Vector.times(num: Int): Vector {
    return Vector(x * num, y * num)
}

operator fun Vector.plusAssign(other: Vector) {
    x += other.x
    y += other.y
}