package com.example.trackspend.ui.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val monthLabels = listOf(
    "JAN","FEB","MAR","APR","MAY","JUN",
    "JUL","AUG","SEP","OCT","NOV","DEC"
)

@Composable
fun YearlyBarChart(
    data: List<Float>,
    maxY: Float,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val textColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val leftPadding = 70f
        val bottomPadding = 60f

        val usableWidth = width - leftPadding
        val usableHeight = height - bottomPadding

        val barWidth = usableWidth / 18f
        val spacing = usableWidth / 12f

        val yStepValue = (maxY / 5f).coerceAtLeast(1f)
        val stepY = usableHeight / maxY

        // GRID
        for (i in 0..5) {
            val yVal = i * yStepValue
            val yPos = height - bottomPadding - (yVal * stepY)

            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(leftPadding, yPos),
                end = androidx.compose.ui.geometry.Offset(width, yPos),
                strokeWidth = 1.dp.toPx()
            )

            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    yVal.roundToInt().toString(),
                    leftPadding - 12.dp.toPx(),
                    yPos + 6.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.copy(alpha = 0.75f).toArgb()
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
        }

        // BARS
        data.forEachIndexed { i, value ->
            val x = leftPadding + i * spacing
            val barHeight = value * stepY
            val top = height - bottomPadding - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }

        // LABELS
        monthLabels.forEachIndexed { index, month ->
            val x = leftPadding + index * spacing

            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    month,
                    x + barWidth / 2,
                    height - 4f,
                    android.graphics.Paint().apply {
                        color = textColor.copy(alpha = 0.8f).toArgb()
                        textSize = 25f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}