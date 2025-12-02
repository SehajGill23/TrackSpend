package com.example.trackspend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trackspend.data.model.TrackingEvent
import com.vipulasri.timelineview.compose.LineStyle
import com.vipulasri.timelineview.compose.Timeline
import com.vipulasri.timelineview.compose.TimelineOrientation
import com.vipulasri.timelineview.compose.getLineType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Renders the full vertical tracking history for a package.
 *
 * The list is:
 *  - sorted by newest event first,
 *  - displayed as a vertical timeline using TimelineView,
 *  - each event rendered inside a styled card.
 *
 * @param events List of tracking events to display
 * @param modifier Optional layout modifier
 */
@Composable
fun TrackingTimeline(
    events: List<TrackingEvent>,
    modifier: Modifier = Modifier
) {
    val sorted = events.sortedByDescending { it.timestamp }

    Column(modifier = modifier.fillMaxWidth()) {

        sorted.forEachIndexed { index, event ->
            TimelineEventCard(event = event, index = index, total = sorted.size)
        }
    }
}


/**
 * Renders a single timeline event row:
 *
 * Structure:
 *  - Left: timeline marker + connecting line (TimelineView)
 *  - Right: glow card showing time, date, status, and location
 *
 * Visual Features:
 *  - Delivered events use green theme (parrot green in light mode)
 *  - Other events use purple brand colors
 *  - Dynamically adapts to dark/light mode
 *
 * @param event Single tracking event
 * @param index Position in the list (needed for timeline line type)
 * @param total Total number of events
 */
@Composable
private fun TimelineEventCard(event: TrackingEvent, index: Int, total: Int) {

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isDelivered = event.status.contains("delivered", ignoreCase = true)

    // --- ORIGINAL DARK-MODE COLORS EXACTLY AS YOU HAD ---
    // --- ORIGINAL DARK-MODE COLORS EXACTLY AS YOU HAD ---
    val cardBg =
        if (isDelivered) {
            if (isDark) Color(0xFF0E2F0E)     // DARK MODE delivered
            else Color(0xFFE6FFE6)           // LIGHT MODE parrot green
        } else if (isDark) Color(0xFF140028)
        else Color(0xFFF3E9FF)

    val borderColor =
        if (isDelivered) Color(0xFF4CAF50)
        else if (isDark) Color.White.copy(alpha = 0.16f)
        else Color.Black.copy(alpha = 0.18f)

    val glow =
        if (isDelivered) {
            if (isDark) Color(0xFF4CAF50).copy(alpha = 0.70f)
            else Color(0xFF4CAF50).copy(alpha = 0.40f) // softer glow for light mode
        } else Color(0xFF9B6DFF).copy(alpha = if (isDark) 0.85f else 0.50f)

    val statusColor =
        if (isDelivered) {
            if (isDark) Color(0xFF84FF84)
            else Color(0xFF008F2A) // brighter visible parrot green text
        } else if (isDark) Color.White
        else Color(0xFF3A2D5C)

    val secondaryColor =
        if (isDelivered) {
            if (isDark) Color(0xFFC2FFC2)
            else Color(0xFF4CAF50) // readable light-green secondary
        } else if (isDark) Color(0xFFBEBEBE)
        else Color(0xFF6A5F80)

    val postalCode = extractPostalCode(event.location)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {

        // --- LEFT TIMELINE ---
        Timeline(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 6.dp),
            lineType = getLineType(index, total),
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

        // --- RIGHT CARD (same spacing, same shape, same glow) ---
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    ambientColor = glow,
                    spotColor = glow,
                    shape = RoundedCornerShape(18.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // --- TIME + DATE ROW (new formatting) ---
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = event.timestamp.toDateStringTimeOnly(),
                        color = secondaryColor,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = event.timestamp.toDateStringDateOnly(),
                        color = secondaryColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // --- STATUS ---
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(
                        imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = event.status,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                // --- LOCATION ---
                if (!event.location.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(6.dp))

                        Column {
                            Text(
                                text = event.location!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryColor
                            )

                            if (postalCode != null) {
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = "Postal Code: $postalCode",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Attempts to extract a postal code from an event's location field.
 *
 * Expected format: "City, PostalCode"
 * Returns null if no comma or postal code found.
 */
private fun extractPostalCode(location: String?): String? {
    return location
        ?.takeIf { it.contains(",") }
        ?.substringAfter(",")
        ?.trim()
}


/**
 * Converts a Unix timestamp (ms) into a user-friendly time string.
 * Example: "3:42 PM"
 */
private fun Long.toDateStringTimeOnly(): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))

/**
 * Converts a Unix timestamp (ms) into a date string.
 * Example: "12 Feb 2025"
 */
private fun Long.toDateStringDateOnly(): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))