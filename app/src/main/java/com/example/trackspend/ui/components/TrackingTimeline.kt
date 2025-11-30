package com.example.trackspend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.trackspend.data.model.TrackingEvent
import com.vipulasri.timelineview.compose.LineStyle
import com.vipulasri.timelineview.compose.Timeline
import com.vipulasri.timelineview.compose.TimelineOrientation
import com.vipulasri.timelineview.compose.getLineType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackingTimeline(
    events: List<TrackingEvent>,
    modifier: Modifier = Modifier
) {
    val sorted = events.sortedByDescending { it.timestamp }

    Column(modifier = modifier.fillMaxWidth()) {

        sorted.forEachIndexed { index, event ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // LEFT: timeline
                Timeline(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 6.dp),
                    lineType = getLineType(index, sorted.size),
                    orientation = TimelineOrientation.Vertical,
                    lineStyle = LineStyle.solid(
                        color = MaterialTheme.colorScheme.primary,
                        width = 3.dp
                    ),
                    marker = {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                    }
                )

                // RIGHT: event card
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (event.timestamp > 0L) {
                            Text(
                                text = event.timestamp.toDateString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        Text(
                            text = event.status,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )

                        if (!event.location.isNullOrEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = event.location!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Long.toDateString(): String {
    return try {
        val sdf = SimpleDateFormat("hh:mm a, dd-MMM-yyyy", Locale.getDefault())
        sdf.format(Date(this))
    } catch (_: Exception) {
        ""
    }
}