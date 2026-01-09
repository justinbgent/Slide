package com.edgeline.slider.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.edgeline.slider.model.Vector
import com.edgeline.slider.model.ccwPerpendicular
import com.edgeline.slider.model.minus
import com.edgeline.slider.model.normalize
import com.edgeline.slider.model.plus
import com.edgeline.slider.model.plusAssign
import com.edgeline.slider.model.rotate
import com.edgeline.slider.model.rotateCw90
import com.edgeline.slider.model.rotationTo
import com.edgeline.slider.model.set
import com.edgeline.slider.model.times
import com.edgeline.slider.model.toVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI

class PlayerSystem {
    private val increaseSpeedAfterPoints = 50
    private val increaseRate = 0.1f
    private val baseSpeed = 300f
    private val maxSpeed = 540f
    private val baseTurnSpeed = PI.toFloat() / 3f

    private val _playerPoints = MutableStateFlow(mutableListOf<Vector>())
    val playerPoints = _playerPoints.asStateFlow()
    val radius = 16f
    private val diameter = radius * 2
    val position = Vector(0f, 0f)
    // Note canvas's offset direction is opposite of player's direction
    private var direction = Vector(0f, -1f)

    // Also the player's start position
    private val playerScreenPosition = Vector(0f, 0f)
    private var speed = baseSpeed
    private var turnSpeed = baseTurnSpeed
    private val goalDirection = Vector(0f, -1f)

    fun initialize(screenWidth: Int, screenHeight: Int) {
        playerScreenPosition.set(screenWidth / 2f, screenHeight / 2f)
        position.set(playerScreenPosition)
        setPlayerDrawingVertices()
        ScoreSystem.subscribeToScore(::onScoreUpdate)
    }

    fun restart() {
        position.set(playerScreenPosition)
        direction = Vector(0f, -1f)
        setPlayerDrawingVertices()
        goalDirection.set(0f, -1f)
        speed = baseSpeed
        turnSpeed = baseTurnSpeed
    }

    fun playerMovement(timeStep: Float): Vector {
        val maxRotation = turnSpeed * timeStep
        val rot = Math.clamp(direction.rotationTo(goalDirection), -maxRotation, maxRotation)
        direction = direction.rotate(rot).normalize()
        val playerMovement = (direction * speed * timeStep)
        position += playerMovement
        setPlayerDrawingVertices()
        return playerMovement
    }

    private fun onScoreUpdate(score: Int) {
        if (speed >= maxSpeed) return
        val factor = 1 + (score / increaseSpeedAfterPoints) * increaseRate
        speed = baseSpeed * factor
        turnSpeed = baseTurnSpeed * factor
    }

    fun tapUpdate(tapPosition: Offset) {
        goalDirection.set((tapPosition).toVector() - playerScreenPosition)
    }

    fun getPlayerDistanceFromStartY(): Int {
        return (position.y - playerScreenPosition.y).toInt()
    }

    private fun setPlayerDrawingVertices(){
        _playerPoints.value.clear()
        val bmPoint = position - direction * (radius)
        val leftVector = direction.ccwPerpendicular()
        val blPoint = bmPoint + leftVector * (radius)
        val tlPoint = blPoint + direction * diameter
        val tmPoint = position + direction * diameter
        val trPoint = tlPoint - leftVector * diameter
        val brPoint = trPoint - direction * diameter

//        val tmPoint = position + direction * (radius * 2)
//        val vector = direction.ccwPerpendicular() + direction
//        val tlPoint = vector * radius
//        vector.rotateCw90()
//        val trPoint = vector * radius
//        vector.rotateCw90()
//        val brPoint = vector * radius
//        vector.rotateCw90()
//        val blPoint = vector * radius

        _playerPoints.value.add(position)
        _playerPoints.value.add(blPoint)
        _playerPoints.value.add(tlPoint)
        _playerPoints.value.add(tmPoint)
        _playerPoints.value.add(trPoint)
        _playerPoints.value.add(brPoint)
    }

    fun onCleared(){
        ScoreSystem.unsubscribeFromScore(::onScoreUpdate)
    }

}