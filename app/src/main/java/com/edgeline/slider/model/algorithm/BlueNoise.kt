package com.edgeline.slider.model.algorithm

import android.util.Log
import com.edgeline.slider.model.Vector2
import com.edgeline.slider.model.minus
import com.edgeline.slider.model.plusAssign
import com.edgeline.slider.model.times
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BlueNoise() {
    /// Cells are divided into two triangles via a diagonal line from
    /// two opposite cell corners. Depending on which diagonal is used, the
    /// below values represent the unused triangle of a cell. None means whole
    /// cell is unused.
    private enum class CellTriangle {
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight,
        None
    }

    private data class Cell(
        var x: Float,
        var y: Float,
        var triangle: CellTriangle
    )

    val cellsPerMinDistance = 1
    // The max index distance from selected cell
    val maxCellDist = 2
    var random = Random(124342)


    /// A rough sampling technique with points a relative distance apart from each other.
    fun sampleRectangle(
        width: Int,
        height: Int,
        goalDistance: Float,
        widthOffset: Int = 0,
        heightOffset: Int = 0
    ): List<Vector2> {
        val points = mutableListOf<Vector2>()
        val cellSize = goalDistance / cellsPerMinDistance
        val halfCellSize = cellSize / 2

        // Column count
        val gridWidth = (width / cellSize).toInt() + 1
        // Row count
        val gridHeight = (height / cellSize).toInt() + 1

        // A list of flattened grid indices, will be swapped around
        val availableCells = mutableListOf<Int>()
        // A map flattenedIndex to availableIndices index that has same flattenedIndex
        val flattenedToAvailable = mutableMapOf<Int, Int>()
        // Grid of cells
        val grid: Array<Array<Cell?>> = Array(gridWidth) { x ->
            Array(gridHeight) { y ->
                val flattenedIndex = y * gridWidth + x
                val position = availableCells.size
                availableCells.add(flattenedIndex)
                flattenedToAvailable[flattenedIndex] = position
                Cell(x * cellSize, y * cellSize, CellTriangle.None)
        }}

        var totalCells = availableCells.size

        while (totalCells > 0) {
            val selection = random.nextInt(0, totalCells)
            val flattenedIndex = availableCells[selection]
            val xIndex = flattenedIndex % gridWidth
            val yIndex = flattenedIndex / gridWidth
            val cell: Cell? = grid[xIndex][yIndex]

            if (cell == null) {
                Log.e("BlueNoise", "Cell is null")
                continue
            }

            // Next, choose point
            val point = Vector2(
                cell.x + random.nextFloat() * cellSize,
                cell.y + random.nextFloat() * cellSize
            )

            // Check if cell's only valid points are in a triangle.
            if (cell.triangle != CellTriangle.None) {
                // Get the point's location relative to cell's origin.
                val cellLocation = Vector2(point.x - cell.x, point.y - cell.y)
                val cellCenter = Vector2(cell.x + halfCellSize, cell.y + halfCellSize)
                // If point is in the wrong triangle, reflect it across cell center. (Its inverse)
                when (cell.triangle) {
                    CellTriangle.TopRight -> {
                        val isInTR = cellLocation.x >= cellLocation.y
                        if (!isInTR) {
                            point += (cellCenter - point) * 2
                        }
                    }
                    CellTriangle.BottomLeft -> {
                        val isInBL = cellLocation.x <= cellLocation.y
                        if (!isInBL) {
                            point += (cellCenter - point) * 2
                        }
                    }
                    CellTriangle.TopLeft -> {
                        val isInTL = cellLocation.x + cellLocation.y <= cellSize
                        if (!isInTL) {
                            point += (cellCenter - point) * 2
                        }
                    }
                    CellTriangle.BottomRight -> {
                        val isInBR = cellLocation.x + cellLocation.y >= cellSize
                        if (!isInBR) {
                            point += (cellCenter - point) * 2
                        }
                    }
                    else -> {}
                }
            }

            // If point is not out of bounds, add to list.
            if (!(point.x > width || point.y > height)) { points.add(point) }

            // Remove the cell, since the cell point is now set.
            grid[xIndex][yIndex] = null
            // Swap with last valid element and decrement
            if (selection < totalCells - 1){
                val valueToMove = availableCells[totalCells - 1]
                availableCells[selection] = valueToMove
                flattenedToAvailable[valueToMove] = selection
            }
            totalCells--

            // https://www.desmos.com/calculator/qib1sld2uk
            // Next, remove the now invalid cells/squares that surround point.
            /* Below is an example of all cells to be removed around a selected cell
               adding triangles where slashes are.
                      /  x  x  x  x  x  \
                   /  x  x  x  x  x  x  x  \
                /  x  x  x  x  x  x  x  x  x  \
                x  x  x  x  x  x  x  x  x  x  x
                x  x  x  x  x  x  x  x  x  x  x
                x  x  x  x  x  O  x  x  x  x  x
                x  x  x  x  x  x  x  x  x  x  x
                x  x  x  x  x  x  x  x  x  x  x
                \  x  x  x  x  x  x  x  x  x  /
                   \  x  x  x  x  x  x  x  /
                      \  x  x  x  x  x  /
               This was a more intensive earlier version. I reduced the size.
            */

            // Dx is index distance from cellX and dy is index distance from cellY
            for (cellX in max(xIndex - cellsPerMinDistance, 0) until
                    min(xIndex + cellsPerMinDistance, gridWidth)) {
                val dx = cellX - xIndex
                for (cellY in max(yIndex - cellsPerMinDistance, 0) until
                        min(yIndex + cellsPerMinDistance, gridHeight)) {

                    // Skip if cell is already removed
                    if (grid[cellX][cellY] == null) { continue }

                    val dy = cellY - yIndex
                    val indexDist = abs(dx) + abs(dy)

                    // Check if cell is in removal range
                    if (indexDist > maxCellDist) { continue }
                    // Determine if this is a corner cell
                    val isInCorner = indexDist == maxCellDist

                    if (isInCorner) // Update triangle instead of removing
                    {
                        val triangleToSet = if (dx < 0) {
                            if (dy > 0) { CellTriangle.BottomLeft }
                            else { CellTriangle.TopLeft }
                        } else{
                            if (dy > 0) { CellTriangle.BottomRight }
                            else { CellTriangle.TopRight }
                        }

                        // Update the cell's valid point placement
                        grid[cellX][cellY]!!.triangle = triangleToSet
                    } else {
                        // This cell is inside range but not on the corner - remove it
                        grid[cellX][cellY] = null
                        val flattenedIndex = cellY * gridWidth + cellX
                        val availableIndex = flattenedToAvailable[flattenedIndex]!!
                        if (availableIndex < totalCells - 1) {
                            val valueToMove = availableCells[totalCells - 1]
                            availableCells[availableIndex] = valueToMove
                            flattenedToAvailable[valueToMove] = availableIndex
                        }
                        totalCells--
                    }
                }
            }
        }

        return points
    }

    fun sampleRectangle(
        width: Int,
        height: Int,
        minimumDistance: Float,
        seed: Int,
        widthOffset: Int = 0,
        heightOffset: Int = 0
    ): List<Vector2> {
        random = Random(seed)
        return sampleRectangle(width, height, minimumDistance, widthOffset, heightOffset)
    }

}