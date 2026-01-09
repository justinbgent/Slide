package com.edgeline.slider.game.system

import com.edgeline.slider.room.model.Score
import com.edgeline.slider.room.dao.ScoreDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.abs

class ScoreSystem(private val scoreDao: ScoreDao) {
    companion object {
        private val onScoreUpdate = mutableListOf<(score: Int) -> Unit>()

        fun subscribeToScore(listener: (score: Int) -> Unit) {
            onScoreUpdate.add(listener)
        }

        fun unsubscribeFromScore(listener: (score: Int) -> Unit) {
            onScoreUpdate.remove(listener)
        }
    }

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()
    private var scoreIncreaseDist = ChunkSystem.Companion.SQUARE_SIZE.toInt()

    fun updateScore(yTravel: Int) {
        val newScore = abs(yTravel) / scoreIncreaseDist
        if (newScore > _score.value) {
            _score.value = newScore
            onScoreUpdate.forEach { it(_score.value) }
        }
    }

    suspend fun saveScore() = withContext(Dispatchers.IO) {
        if (_score.value > 0) {
            scoreDao.insertAndCleanup(Score(score = _score.value))
        }
    }

    fun restart() {
        _score.value = 0
    }

}
