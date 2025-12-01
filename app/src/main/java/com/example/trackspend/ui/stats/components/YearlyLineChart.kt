package com.example.trackspend.ui.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val monthLabels = listOf(
    "JAN","FEB","MAR","APR","MAY","JUN",
    "JUL","AUG","SEP","OCT","NOV","DEC"
)

@Composable
fun YearlyLineChart(
    data: List<Float>,
    maxY: Float,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val leftPadding = 70f     // space for y labels
        val bottomPadding = 60f   // space for month labels

        val usableWidth = width - leftPadding
        val usableHeight = height - bottomPadding

        val stepX = usableWidth / 11f   // 12 months (0–11)
        val stepY = usableHeight / maxY

        // -------------------- GRID LINES --------------------
        val yStepValue = (maxY / 5f).coerceAtLeast(1f)
        for (i in 0..5) {
            val yVal = i * yStepValue
            val yPos = height - bottomPadding - (yVal * stepY)

            drawLine(
                color = gridColor,
                start = Offset(leftPadding, yPos),
                end = Offset(width, yPos),
                strokeWidth = 1.dp.toPx()
            )

            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    yVal.roundToInt().toString(),
                    leftPadding - 12.dp.toPx(),
                    yPos + 6.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
        }

        // -------------------- X-AXIS MONTH LABELS --------------------
        monthLabels.forEachIndexed { index, month ->
            val x = leftPadding + index * stepX
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    month,
                    x,
                    height - 4f,
                    android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 25f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        // --------------------- LINE PATH ---------------------
        val path = Path()
        data.forEachIndexed { i, value ->
            val x = leftPadding + i * stepX
            val y = height - bottomPadding - (value * stepY)

            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        // gradient under-fill
        val fillPath = Path().apply {
            addPath(path)
            lineTo(leftPadding + 11 * stepX, height - bottomPadding)
            lineTo(leftPadding, height - bottomPadding)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                0f to lineColor.copy(alpha = 0.25f),
                1f to lineColor.copy(alpha = 0f)
            ),
            style = Fill
        )

        // outline line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4.dp.toPx())
        )

        // points
        data.forEachIndexed { i, value ->
            val x = leftPadding + i * stepX
            val y = height - bottomPadding - (value * stepY)

            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}