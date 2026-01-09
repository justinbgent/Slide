package com.edgeline.slider.viewmodel

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgeline.slider.game.system.CameraSystem
import com.edgeline.slider.game.system.ChunkSystem
import com.edgeline.slider.game.system.CollisionSystem
import com.edgeline.slider.game.system.PlayerSystem
import com.edgeline.slider.game.system.ScoreSystem
import com.edgeline.slider.game.model.ChunkData
import com.edgeline.slider.game.model.Vector
import com.edgeline.slider.room.dao.ScoreDao
import com.edgeline.slider.room.model.Score
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class GameViewModel(private val scoreDao: ScoreDao) : ViewModel() {
    private val chunkSystem = ChunkSystem()
    private val camera = CameraSystem()
    private val collisionSystem = CollisionSystem()
    private val scoreSystem = ScoreSystem(scoreDao)
    private val player = PlayerSystem()

    private val _chunkData = MutableStateFlow(listOf<ChunkData>())
    val chunkData: StateFlow<List<ChunkData>> = _chunkData.asStateFlow()
    private val _isGameOver = MutableStateFlow(false)
    val isGameOver = _isGameOver.asStateFlow()
    val score: StateFlow<Int>
        get() = scoreSystem.score
    val canvasOffset: StateFlow<Vector>
        get() = camera.canvasOffset
    val playerPoints: StateFlow<MutableList<Vector>>
        get() = player.playerPoints
    val highScores: StateFlow<List<Score>> = scoreSystem.getScores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var endRectPos = Offset.Zero
    private var frameTimestamp = 0L

    fun initialize(width: Int, height: Int) {
        player.initialize(width, height)
        chunkSystem.initialize(
            width, height,
            player.position.y
        )
    }

    suspend fun gameLoop(now: Long) {
        if (_isGameOver.value) return
        // Don't calculate movement on the first frame
        if (frameTimestamp == 0L) {
            frameTimestamp = now
            return
        }

        if (isGameOver()) {
            scoreSystem.saveScore()
            _isGameOver.value = true
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

    fun onTap(tapPosition: Offset) {
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
        val collisionOffset = collisionSystem.checkPlayerCollision(player.position, player.radius)
        if (collisionOffset != null) {
            Log.i(GameViewModel::class.simpleName, "Collision at $collisionOffset, Position${player.position}")
            endRectPos = collisionOffset
            return true
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        player.onCleared()
    }
}
