package com.edgeline.slider.game

import com.edgeline.slider.game.ChunkSystem.Companion.SQUARE_SIZE
import kotlin.math.abs

class ScoreSystem {
    val score: Int
        get() = _score
    private var _score = 0
    private var scoreIncreaseDist = SQUARE_SIZE.toInt()

    fun updateScore(yTravel: Int) {
        val newScore = abs(yTravel) / scoreIncreaseDist
        if (newScore > _score) {
            _score = newScore
        }
    }
}