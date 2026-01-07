package com.edgeline.slider.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.edgeline.slider.game.ChunkSystem.Companion.SQUARE_SIZE
import com.edgeline.slider.game.ChunkSystem.Companion.boundary1Coord
import com.edgeline.slider.game.ChunkSystem.Companion.boundary2Coord
import com.edgeline.slider.game.ChunkSystem.Companion.BOUNDARY_WIDTH
import com.edgeline.slider.game.ChunkSystem.Companion.CHUNK_HEIGHT
import com.edgeline.slider.model.OffsetPoints
import com.edgeline.slider.model.Rectangle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollisionSystem {
    private var currentRectangles = mutableListOf<Rectangle>()

    fun restart() {
        currentRectangles = mutableListOf()
    }

    suspend fun update(offsetPoints: OffsetPoints) {
        withContext(Dispatchers.IO) {
            val rectangles = mutableListOf<Rectangle>()
            for (point in offsetPoints.points) {
                rectangles.add(
                    Rectangle(
                        point.x + offsetPoints.pointOffset.x,
                        point.y + offsetPoints.pointOffset.y,
                        SQUARE_SIZE,
                        SQUARE_SIZE
                    )
                )
            }
            rectangles.add(
                Rectangle(
                    boundary1Coord.x + offsetPoints.pointOffset.x,
                    boundary1Coord.y + offsetPoints.pointOffset.y,
                    BOUNDARY_WIDTH,
                    CHUNK_HEIGHT.toFloat()
                )
            )
            rectangles.add(
                Rectangle(
                    boundary2Coord.x + offsetPoints.pointOffset.x,
                    boundary2Coord.y + offsetPoints.pointOffset.y,
                    BOUNDARY_WIDTH,
                    CHUNK_HEIGHT.toFloat()
                )
            )
            currentRectangles = rectangles
        }
    }

    suspend fun checkPlayerCollision(playerPosition: Offset, playerSize: Size): Offset? {
        return withContext(Dispatchers.Default) {
            val player =
                Rectangle(
                    playerPosition.x,
                    playerPosition.y,
                    playerSize.width,
                    playerSize.height
                )
            for (rect in currentRectangles) {
                if (rect.Intersects(player)) {
                    return@withContext Offset(rect.left, rect.top)
                }
            }
            return@withContext null
        }
    }
}