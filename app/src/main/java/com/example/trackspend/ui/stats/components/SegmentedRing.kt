package com.example.trackspend.ui.stats.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SegmentedRing(
    segments: Map<String, Int>,
    total: Int,
    modifier: Modifier = Modifier.size(160.dp),
    strokeWidth: Float = 32f,
    showLabels: Boolean = true

) {
    if (total == 0 || segments.isEmpty()) return

    val storeColors = listOf(
        Color(0xFF4285F4), // Blue
        Color(0xFFFBBC05), // Yellow
        Color(0xFF34A853), // Green
        Color(0xFFEA4335), // Red
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF7043), // Orange
        Color(0xFF29B6F6), // Light Blue
        Color(0xFFAB47BC), // Soft Purple
        Color(0xFF66BB6A), // Soft Green
        Color(0xFFFFCA28), // Amber
        Color(0xFFEF5350), // Soft Red
        Color(0xFF5C6BC0), // Indigo
        Color(0xFF26C6DA), // Cyan
        Color(0xFFFF8A65), // Coral
        Color(0xFFD4E157), // Lime
        Color(0xFF7E57C2)  // Deep Purple
    )

    val segmentList = segments.entries.toList()
    val animatedSweep by animateFloatAsState(targetValue = 1f, label = "")

    Canvas(modifier = modifier) {

        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = minOf(canvasWidth, canvasHeight) / 2f
        val labelRadius = radius + 100f    // <-- keeps labels OUTSIDE

        var startAngle = -90f

        segmentList.forEachIndexed { index, entry ->

            val percentage = entry.value.toFloat() / total.toFloat()
            val sweep = percentage * 360f * animatedSweep
            val midAngle = startAngle + sweep / 2f
            val rad = Math.toRadians(midAngle.toDouble())

            // ----- DRAW SEGMENT -----
            drawArc(
                color = storeColors[index % storeColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(0f, 0f),
                size = Size(canvasWidth, canvasHeight)
            )

            // ----- CALCULATE LABEL POSITION -----
            if (showLabels) {
                val labelX = center.x + cos(rad).toFloat() * labelRadius
                val labelY = center.y + sin(rad).toFloat() * labelRadius

                // ----- DRAW LABEL (SOFT COLOR) -----
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        entry.key,
                        labelX,
                        labelY,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(180, 255, 255, 255) // soft white
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            startAngle += sweep
        }
    }
}