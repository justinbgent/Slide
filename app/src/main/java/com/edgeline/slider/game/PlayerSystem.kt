package com.edgeline.slider.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.edgeline.slider.model.Vector
import com.edgeline.slider.model.normalize
import com.edgeline.slider.model.rotate
import com.edgeline.slider.model.rotationTo
import com.edgeline.slider.model.times
import com.edgeline.slider.model.toOffset
import com.edgeline.slider.model.toVector
import kotlin.math.PI

class PlayerSystem {
    val playerSize = Size(32f, 32f)
    val playerCenter = Offset(playerSize.width / 2, playerSize.height / 2)
    val playerPosition: Offset
        get() = _playerPosition
    val direction: Offset
        get() = _direction.toOffset()
    // Also the player's start position
    private var playerScreenPosition = Offset.Zero
    private var _playerPosition = Offset.Zero
    private val speed = 300f
    private val turnSpeed = PI.toFloat() / 3f
    // Note canvas's offset direction is opposite of player's direction
    private var _direction = Vector(0f, -1f)
    private var goalDirection = Vector(0f, 0f)

    fun initialize(screenWidth: Int, screenHeight: Int){
        playerScreenPosition = Offset(screenWidth / 2f, screenHeight / 2f) - playerCenter
        _playerPosition = playerScreenPosition
    }

    fun playerMovement(timeStep: Float): Offset {
        val maxRotation = turnSpeed * timeStep
        val rot = Math.clamp(_direction.rotationTo(goalDirection), -maxRotation, maxRotation)
        _direction = _direction.rotate(rot).normalize()
        val playerMovement = (_direction * speed * timeStep).toOffset()
        _playerPosition += playerMovement
        return playerMovement
    }

    fun tapUpdate(tapPosition: Offset) {
        goalDirection = (tapPosition - playerScreenPosition).toVector()
    }

    fun getPlayerDistanceFromStartY(): Int {
        return (playerPosition.y - playerScreenPosition.y).toInt()
    }

}