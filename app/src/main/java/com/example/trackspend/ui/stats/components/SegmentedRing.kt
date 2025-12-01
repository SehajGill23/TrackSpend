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
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedRing(
    segments: Map<String, Int>,
    total: Int,
    modifier: Modifier = Modifier.size(160.dp),
    strokeWidth: Float = 32f
) {
    if (total == 0 || segments.isEmpty()) return

    // Colors for each store
    val storeColors = listOf(
        Color(0xFF4285F4), // blue
        Color(0xFFFBBC05), // yellow
        Color(0xFF34A853), // green
        Color(0xFFEA4335), // red
        Color(0xFF9C27B0), // purple
        Color(0xFFFF7043), // orange
        Color(0xFF29B6F6), // cyan
    )

    val segmentList = segments.entries.toList()

    // Animate sweep angles
    val animatedSweep by animateFloatAsState(targetValue = 1f, label = "")

    Canvas(modifier = modifier) {
        var startAngle = -90f

        segmentList.forEachIndexed { index, entry ->
            val percentage = entry.value.toFloat() / total.toFloat()
            val sweep = percentage * 360f * animatedSweep

            drawArc(
                color = storeColors[index % storeColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height)
            )

            startAngle += sweep
        }
    }
}