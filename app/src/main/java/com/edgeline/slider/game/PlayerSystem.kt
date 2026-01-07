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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI

class PlayerSystem {
    val playerSize = Size(32f, 32f)
    val playerCenter = Offset(playerSize.width / 2, playerSize.height / 2)
    private val _playerPosition = MutableStateFlow(Offset.Zero)
    val playerPosition = _playerPosition.asStateFlow()
    // Note canvas's offset direction is opposite of player's direction
    private val _direction = MutableStateFlow(Vector(0f, -1f))
    val direction = _direction.asStateFlow()
    // Also the player's start position
    private var playerScreenPosition = Offset.Zero
    private val speed = 300f
    private val turnSpeed = PI.toFloat() / 3f
    private var goalDirection = Vector(0f, -1f)

    fun initialize(screenWidth: Int, screenHeight: Int){
        playerScreenPosition = Offset(screenWidth / 2f, screenHeight / 2f) - playerCenter
        _playerPosition.value = playerScreenPosition
    }

    fun restart() {
        _playerPosition.value = playerScreenPosition
        _direction.value = Vector(0f, -1f)
        goalDirection = Vector(0f, -1f)
    }

    fun playerMovement(timeStep: Float): Offset {
        val maxRotation = turnSpeed * timeStep
        val rot = Math.clamp(_direction.value.rotationTo(goalDirection), -maxRotation, maxRotation)
        _direction.value = _direction.value.rotate(rot).normalize()
        val playerMovement = (_direction.value * speed * timeStep).toOffset()
        _playerPosition.value += playerMovement
        return playerMovement
    }

    fun tapUpdate(tapPosition: Offset) {
        goalDirection = (tapPosition - playerScreenPosition).toVector()
    }

    fun getPlayerDistanceFromStartY(): Int {
        return (_playerPosition.value.y - playerScreenPosition.y).toInt()
    }

}