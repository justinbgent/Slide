package com.edgeline.slider.game

import com.edgeline.slider.game.ChunkSystem.Companion.SQUARE_SIZE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class ScoreSystem {
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
    private var scoreIncreaseDist = SQUARE_SIZE.toInt()

    fun updateScore(yTravel: Int) {
        val newScore = abs(yTravel) / scoreIncreaseDist
        if (newScore > _score.value) {
            _score.value = newScore
            onScoreUpdate.forEach { it(_score.value) }
        }
    }

    fun restart() {
        _score.value = 0
    }

}