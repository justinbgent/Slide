package com.edgeline.slider.model.algorithm

import android.util.Log
import com.edgeline.slider.model.Vector2
import com.edgeline.slider.model.minus
import com.edgeline.slider.model.plusAssign
import com.edgeline.slider.model.times
import kotlin.math.abs
import kotlin.random.Random

class BlueNoise() {
    /// <summary>Cells are divided into two triangles via a diagonal line from
    /// two opposite cell corners. Depending on which diagonal is used, the
    /// below values represent the unused triangle of a cell. None means whole
    /// cell is unused.</summary>
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

    val cellsPerUnit = 5
    var random = Random(124342)


    /// <summary>Making a rough sampling technique with points a specific distance
    /// apart from each other.</summary>
    /// <param name="width"></param>
    /// <param name="height"></param>
    /// <param name="minimumDistance"></param>
    /// <returns></returns>
    fun sampleRectangle(
        width: Int,
        height: Int,
        minimumDistance: Float,
        widthOffset: Int = 0,
        heightOffset: Int = 0
    ): List<Vector2> {
        val points = mutableListOf<Vector2>()
        val cellSize = minimumDistance / cellsPerUnit
        val halfCellSize = cellSize / 2

        // Column count
        val gridWidth = (width / cellSize).toInt() + 1
        // Row count
        val gridHeight = (height / cellSize).toInt() + 1
        var totalCells = gridWidth * gridHeight

        val grid = mutableMapOf<Int, MutableMap<Int, Cell>>()
        for (i in 0..<totalCells) {
            val x = i % gridWidth
            val y = i / gridWidth
            if (!grid.containsKey(x)) {
                grid[x] = mutableMapOf<Int, Cell>()
            }
            grid[x]!![y] = Cell(x * cellSize, y * cellSize, CellTriangle.None)
        }

        while (totalCells > 0) {
            val selection = random.nextInt(0, totalCells)
            var cell: Cell? = null
            var counter = 0
            var xIndex = 0
            var yIndex = 0
            // Iterate to find cell that represents selection number.
            for (column in grid) {
                val columnCount = column.value.size
                // If selection is less than cells that will be counted over,
                // then cell is inside this column.
                if (columnCount + counter > selection) {
                    // Iterate through cell pairs.
                    for (item in column.value) {
                        // If counter matches selection, then this is the cell.
                        if (counter == selection) {
                            cell = item.value
                            xIndex = column.key
                            yIndex = item.key
                            break
                        }
                        counter++
                    }
                    // Break out of the loop once the selected cell is retrieved.
                    break
                } else {
                    // Add items in the column to the count.
                    counter += columnCount
                }
            }

            // If cell was never initialized,
            if (cell == null) {
                Log.i("BlueNoise", "Failed to retrieve cell. Something went wrong.")
                continue
            }

            // Next, choose point
            val x = cell.x + random.nextFloat() * cellSize
            val y = cell.y + random.nextFloat() * cellSize
            val point = Vector2(x, y)

            // Check if cell's only valid points are in a triangle.
            if (cell.triangle != CellTriangle.None) {
                // Get the point's location relative to cell's origin.
                val cellLocation = Vector2(x - cell.x, y - cell.y)
                // Get the cell's center.
                val cellCenter = Vector2(cell.x + halfCellSize, cell.y + halfCellSize)
                // This assumes Y increases going upward.
                // Values will need to change if Y increases downward.
                val isInTR = cellLocation.x >= cellLocation.y
                val isInBL = cellLocation.x <= cellLocation.y
                val isInTL = cellLocation.x + cellLocation.y <= cellSize
                val isInBR = cellLocation.x + cellLocation.y >= cellSize
                // If point is in the wrong triangle, reflect it across cell center. (Its inverse)
                when (cell.triangle) {
                    CellTriangle.TopRight ->
                        if (!isInTR) {
                            point += (cellCenter - point) * 2
                        }

                    CellTriangle.BottomLeft ->
                        if (!isInBL) {
                            point += (cellCenter - point) * 2
                        }

                    CellTriangle.TopLeft ->
                        if (!isInTL) {
                            point += (cellCenter - point) * 2
                        }

                    CellTriangle.BottomRight ->
                        if (!isInBR) {
                            point += (cellCenter - point) * 2
                        }

                    else -> {}
                }
            }

            // If point is not out of bounds, add to list.
            if (!(x > width || y > height)) {
                points.add(point)
            }
            // Remove the cell, since the cell point is now set.
            removeCell(grid, xIndex, yIndex)
            totalCells--

            // https://www.desmos.com/calculator/qib1sld2uk
            // Next, remove the now invalid cells/squares that surround point.
            /* Below is an example of all cells to be removed around a selected cell.
               "O" is the selected cell below. Note TopLeft, TopRight, BottomLeft, and
               BottomRight are indicated with slashes in which cells will not be removed
               but will instead have the Cell.Triangle field updated with the respective
               triangle it would be. These represent the grid cells. Using
               xIndex and yIndex will get the center cell.
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
            */

            // The max index distance from selected cell
            val maxCellDist = 8

            // Iterate through all cells within the diamond pattern
            // Dx is index distance from cell.X and dy is index distance from cell.Y
            for (dx in -cellsPerUnit..cellsPerUnit) {
                for (dy in -cellsPerUnit..cellsPerUnit) {
                    // Check if cell is in range
                    val indexDist = abs(dx) + abs(dy)
                    if (indexDist > maxCellDist) {
                        continue
                    }

                    val cellX = xIndex + dx
                    val cellY = yIndex + dy

                    // Skip if cell doesn't exists in grid
                    if (!grid.containsKey(cellX) || !grid[cellX]!!.containsKey(cellY)) {
                        continue
                    }

                    // Determine if this is a corner cell
                    val isInCorner = indexDist == maxCellDist

                    if (isInCorner) // Update triangle instead of removing
                    {
                        val triangleToSet = if (dx < 0 && dy > 0) {
                            CellTriangle.TopLeft
                        } else if (dx > 0 && dy > 0) {
                            CellTriangle.TopRight
                        } else if (dx < 0 && dy < 0) {
                            CellTriangle.BottomLeft
                        } else if (dx > 0 && dy < 0) {
                            CellTriangle.BottomRight
                        } else {
                            CellTriangle.None
                        }

                        // Update the cell's valid point placement
                        grid[cellX]!![cellY]!!.triangle = triangleToSet
                    } else {
                        // This cell is inside the diamond but not on the edge - remove it
                        removeCell(grid, cellX, cellY)
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

    private fun removeCell(grid: MutableMap<Int, MutableMap<Int, Cell>>, cellX: Int, cellY: Int) {
        grid[cellX]!!.remove(cellY)
        if (grid[cellX]!!.isEmpty()) {
            grid.remove(cellX)
        }
    }
}