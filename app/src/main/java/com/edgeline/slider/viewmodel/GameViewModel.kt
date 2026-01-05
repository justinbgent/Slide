package com.edgeline.slider.viewmodel

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import com.edgeline.slider.game.ChunkSystem
import com.edgeline.slider.game.CollisionSystem
import com.edgeline.slider.game.CameraSystem
import com.edgeline.slider.game.PlayerSystem
import com.edgeline.slider.game.ScoreSystem
import com.edgeline.slider.model.ChunkData

class GameViewModel() : ViewModel() {
    val playerSize: Size
        get() = player.playerSize
    val playerCenter: Offset
        get() = player.playerCenter
    val playerPosition: Offset
        get() = player.playerPosition
    val score: Int
        get() = scoreSystem.score
    val direction: Offset
        get() = player.direction
    val canvasOffset: Offset
        get() = camera.canvasOffset

    private val chunkSystem = ChunkSystem()
    private val camera = CameraSystem()
    private val collisionSystem = CollisionSystem()
    private val scoreSystem = ScoreSystem()
    private val player = PlayerSystem()

    var endRectPos = Offset.Zero

    fun initialize(width: Int, height: Int) {
        player.initialize(width, height)
        chunkSystem.initialize(width, height, playerPosition.y)
    }

    fun tapVector(tapPosition: Offset) {
        player.tapUpdate(tapPosition)
    }

    suspend fun updateSystems(deltaTime: Long): List<ChunkData>? {
        val timeStep = deltaTime / 1000f
        val playerMovement = player.playerMovement(timeStep)
        camera.updateCameraState(playerMovement)
        val distanceFromStart = player.getPlayerDistanceFromStartY()
        scoreSystem.updateScore(distanceFromStart)
        val data = chunkSystem.updateAndGetChunksIfNeeded(distanceFromStart)
        if (data != null) {
            collisionSystem.update(chunkSystem.getChunkPoints())
        }
        return data
    }

    suspend fun isGameOver(): Boolean {
        val collisionOffset = collisionSystem.checkPlayerCollision(playerPosition, playerSize)
        if (collisionOffset != null) {
            endRectPos = collisionOffset
            Log.i(GameViewModel::class.simpleName, "Player position $playerPosition, intersected ${endRectPos.x}, ${endRectPos.y}")
            return true
        }
        return false
    }
}
//    val dirtGroundColor = Color(150, 111, 67, 255)