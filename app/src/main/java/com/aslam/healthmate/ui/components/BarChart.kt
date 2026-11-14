package com.aslam.healthmate.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Simple bar chart drawn by hand on a Canvas, with a dashed line for the goal.
// No chart library needed.
@Composable
fun BarChart(
    values: List<Int>,
    labels: List<String>,
    goal: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.onSurface
    // leave a bit of room above the tallest bar or the goal line
    val maxValue = maxOf(values.maxOrNull() ?: 0, goal, 1) * 1.1f

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val slotWidth = size.width / values.size
            val barWidth = slotWidth * 0.6f

            values.forEachIndexed { i, value ->
                val barHeight = size.height * (value / maxValue)
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(i * slotWidth + (slotWidth - barWidth) / 2, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }

            // Goal line
            val goalY = size.height - size.height * (goal / maxValue)
            drawLine(
                color = lineColor,
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
            )
        }
        // Day labels under the bars
        Row(Modifier.fillMaxWidth()) {
            labels.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
