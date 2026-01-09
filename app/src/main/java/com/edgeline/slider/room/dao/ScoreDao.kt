package com.edgeline.slider.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.edgeline.slider.room.model.Score
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Upsert
    suspend fun upsertScore(score: Score)

    @Query("DELETE FROM scores WHERE score NOT IN (SELECT score FROM scores ORDER BY score DESC LIMIT 10)")
    suspend fun deleteOldScores()

    @Transaction
    suspend fun insertAndCleanup(score: Score) {
        upsertScore(score)
        deleteOldScores()
    }

    @Delete
    suspend fun deleteScore(score: Score)

    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 10")
    fun getAllScores(): Flow<List<Score>>

    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 10")
    suspend fun getAllScoresList(): List<Score>

    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 1")
    fun getHighScore(): Flow<Score?>
}
