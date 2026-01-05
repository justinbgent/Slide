package com.edgeline.slider.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.edgeline.slider.game.ChunkSystem.Companion.SQUARE_SIZE
import com.edgeline.slider.model.OffsetPoints
import com.edgeline.slider.model.Rectangle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollisionSystem {
    private var currentRectangles = mutableListOf<Rectangle>()

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