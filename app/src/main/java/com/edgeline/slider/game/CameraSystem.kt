package com.edgeline.slider.game

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// So when I translate the canvas pausitively, I'm shifting the grid down.
// Thus, more of the grid's negatives are visible and I need to draw to those.
class CameraSystem {
    private val _canvasOffset = MutableStateFlow(Offset.Zero)
    val canvasOffset = _canvasOffset.asStateFlow()

    fun updateCameraState(playerMovement: Offset) {
        // Subtract playerMovement to move canvas appropriately
        //  This will maintain player's position on the camera
        _canvasOffset.value -= playerMovement
    }

    fun restart() {
        _canvasOffset.value = Offset.Zero
    }

}