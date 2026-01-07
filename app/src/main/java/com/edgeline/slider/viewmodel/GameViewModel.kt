package com.edgeline.slider.viewmodel

import android.util.Log
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgeline.slider.game.ChunkSystem
import com.edgeline.slider.game.CollisionSystem
import com.edgeline.slider.game.CameraSystem
import com.edgeline.slider.game.PlayerSystem
import com.edgeline.slider.game.ScoreSystem
import com.edgeline.slider.model.ChunkData
import com.edgeline.slider.model.Vector
import com.edgeline.slider.model.toOffset
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameViewModel() : ViewModel() {
    private val chunkSystem = ChunkSystem()
    private val camera = CameraSystem()
    private val collisionSystem = CollisionSystem()
    private val scoreSystem = ScoreSystem()
    private val player = PlayerSystem()

    private val _chunkData = MutableStateFlow(listOf<ChunkData>())
    val chunkData: StateFlow<List<ChunkData>> = _chunkData.asStateFlow()
    private val _isGameOver = MutableStateFlow(false)
    val isGameOver = _isGameOver.asStateFlow()
    val score: StateFlow<Int>
        get() = scoreSystem.score
    val canvasOffset: StateFlow<Offset>
        get() = camera.canvasOffset
    val playerSize: Size
        get() = player.playerSize
    val playerPosition: StateFlow<Offset>
        get() = player.playerPosition
    val direction: StateFlow<Vector>
        get() = player.direction
    val playerCenter: Offset
        get() = player.playerCenter

    var endRectPos = Offset.Zero
    private var frameTimestamp = 0L

    fun initialize(width: Int, height: Int) {
        player.initialize(width, height)
        chunkSystem.initialize(
            width, height,
            playerPosition.value.y
        )
    }

    suspend fun gameLoop(now: Long) {
        // Don't calculate movement on the first frame
        if (frameTimestamp == 0L) {
            frameTimestamp = now
            return
        }

        if (!_isGameOver.value) {
            _isGameOver.value = isGameOver()
        }
        else {
            return
        }


        // Update chunks
        val newVisuals = updateSystems(now - frameTimestamp)
        if (newVisuals != null) {
            _chunkData.value = newVisuals
        }

        frameTimestamp = now
    }

    fun restartGame() {
        if (_isGameOver.value) {
            chunkSystem.restart()
            camera.restart()
            collisionSystem.restart()
            scoreSystem.restart()
            player.restart()
            _chunkData.value = listOf()
            _isGameOver.value = false
            frameTimestamp = 0L
        }
    }

    fun tapVector(tapPosition: Offset) {
        player.tapUpdate(tapPosition)
    }

    suspend fun updateSystems(deltaTime: Long): List<ChunkData>? {
        val timeStep = deltaTime / 1000f
        if (timeStep > 0.01f)
        Log.i(GameViewModel::class.simpleName, "$timeStep")
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
        val collisionOffset = collisionSystem.checkPlayerCollision(playerPosition.value, playerSize)
        if (collisionOffset != null) {
            endRectPos = collisionOffset
            return true
        }
        return false
    }
}
//    val dirtGroundColor = Color(150, 111, 67, 255)