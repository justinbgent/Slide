package com.edgeline.slider.game.system

import com.edgeline.slider.game.model.Vector
import com.edgeline.slider.game.model.minus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// So when I translate the canvas pausitively, I'm shifting the grid down.
// Thus, more of the grid's negatives are visible and I need to draw to those.
class CameraSystem {
    private val _canvasOffset = MutableStateFlow(Vector(0f, 0f))
    val canvasOffset = _canvasOffset.asStateFlow()

    fun updateCameraState(playerMovement: Vector) {
        // Subtract playerMovement to move canvas appropriately
        //  This will maintain player's position on the camera
        _canvasOffset.value -= playerMovement
    }

    fun restart() {
        _canvasOffset.value = Vector(0f, 0f)
    }

}