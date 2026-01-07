package com.edgeline.slider.game

import com.edgeline.slider.game.ChunkSystem.Companion.SQUARE_SIZE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class ScoreSystem {
    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()
    private var scoreIncreaseDist = SQUARE_SIZE.toInt()

    fun updateScore(yTravel: Int) {
        val newScore = abs(yTravel) / scoreIncreaseDist
        if (newScore > _score.value) {
            _score.value = newScore
        }
    }

    fun restart() {
        _score.value = 0
    }

}