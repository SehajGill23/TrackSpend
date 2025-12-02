package com.example.trackspend.ui.stats.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


/**
 * A minimal, modern thin toggle switch used in stats filters.
 *
 * Behaviors:
 *  - Smooth thumb animation using `animateDpAsState`
 *  - Clickable track that flips the boolean state
 *  - Compact “thin” design (smaller than a standard switch)
 *
 * @param checked current switch state
 * @param onCheckedChange callback invoked when the user toggles the switch
 * @param modifier optional layout modifier
 */
@Composable
fun ThinToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackWidth = 38.dp
    val trackHeight = 18.dp  //  THIN
    val thumbSize = 14.dp    //  SMALL

    val offset by animateDpAsState(
        targetValue = if (checked) (trackWidth - thumbSize - 4.dp) else 4.dp
    )

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(CircleShape)
            .background(
                if (checked)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = offset, y = 2.dp)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}