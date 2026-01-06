package com.edgeline.slider.game

import androidx.compose.ui.geometry.Offset

// So when I translate the canvas pausitively, I'm shifting the grid down.
// Thus, more of the grid's negatives are visible and I need to draw to those.
class CameraSystem {
    val canvasOffset: Offset
        get() = _canvasOffset
    private var _canvasOffset = Offset.Zero

    fun updateCameraState(playerMovement: Offset) {
        // Subtract playerMovement to move canvas appropriately
        //  This will maintain player's position on the camera
        _canvasOffset -= playerMovement
    }
}