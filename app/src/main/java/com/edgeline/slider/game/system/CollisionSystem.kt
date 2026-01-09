package com.edgeline.slider.game.system

import androidx.compose.ui.geometry.Offset
import com.edgeline.slider.game.model.Circle
import com.edgeline.slider.game.model.OffsetPoints
import com.edgeline.slider.game.model.Rectangle
import com.edgeline.slider.game.model.Vector
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
                        ChunkSystem.Companion.SQUARE_SIZE,
                        ChunkSystem.Companion.SQUARE_SIZE
                    )
                )
            }
            rectangles.add(
                Rectangle(
                    ChunkSystem.Companion.boundary1Coord.x + offsetPoints.pointOffset.x,
                    ChunkSystem.Companion.boundary1Coord.y + offsetPoints.pointOffset.y,
                    ChunkSystem.Companion.BOUNDARY_WIDTH,
                    ChunkSystem.Companion.CHUNK_HEIGHT.toFloat()
                )
            )
            rectangles.add(
                Rectangle(
                    ChunkSystem.Companion.boundary2Coord.x + offsetPoints.pointOffset.x,
                    ChunkSystem.Companion.boundary2Coord.y + offsetPoints.pointOffset.y,
                    ChunkSystem.Companion.BOUNDARY_WIDTH,
                    ChunkSystem.Companion.CHUNK_HEIGHT.toFloat()
                )
            )
            currentRectangles = rectangles
        }
    }

    suspend fun checkPlayerCollision(position: Vector, radius: Float): Offset? {
        return withContext(Dispatchers.Default) {
            val player = Circle(position, radius)
            for (rect in currentRectangles) {
                if (player.intersects(rect)) {
                    return@withContext Offset(rect.left, rect.top)
                }
            }
            return@withContext null
        }
    }
}