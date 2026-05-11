package com.workoutleveling.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.workoutleveling.app.ui.progress.TrendDayUi

@Composable
fun SessionTrendChart(
    points: List<TrendDayUi>,
    modifier: Modifier = Modifier,
) {
    val max = (points.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary
    Column(modifier = modifier) {
        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp, end = 6.dp)
                    .size(10.dp)
                    .background(barColor, CircleShape),
            )
            Text(
                "Sesi harian 14 hari (maks: $max)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            if (points.isEmpty()) return@Canvas
            val n = points.size
            val gap = size.width * 0.02f
            val barW = (size.width - gap * (n + 1)) / n
            points.forEachIndexed { i, p ->
                val h = (p.count.toFloat() / max) * size.height * 0.82f
                val x = gap + i * (barW + gap)
                val y = size.height * 0.92f - h
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barW, h.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }
        }
    }
}
