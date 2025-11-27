package xyz.crearts.activebreak.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

data class ChartData(
    val label: String,
    val value: Float
)

@Composable
fun LineChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    title: String = "",
    lineColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет данных",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            return@Column
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            drawLineChart(
                data = data,
                lineColor = lineColor,
                backgroundColor = backgroundColor,
                textColor = textColor
            )
        }

        // Labels for X-axis
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun DrawScope.drawLineChart(
    data: List<ChartData>,
    lineColor: Color,
    backgroundColor: Color,
    textColor: Color
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val valueRange = max(maxValue - minValue, 1f)

    val width = size.width
    val height = size.height
    val padding = 40f

    // Draw background grid
    val gridColor = textColor.copy(alpha = 0.1f)
    val gridLines = 5
    for (i in 0..gridLines) {
        val y = padding + (height - 2 * padding) * i / gridLines
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Calculate points
    val points = data.mapIndexed { index, point ->
        val x = padding + (width - 2 * padding) * index / (data.size - 1).coerceAtLeast(1)
        val normalizedValue = if (valueRange > 0) (point.value - minValue) / valueRange else 0f
        val y = height - padding - (height - 2 * padding) * normalizedValue
        Offset(x, y)
    }

    // Draw gradient fill under the line
    if (points.size > 1) {
        val path = Path().apply {
            moveTo(points.first().x, height - padding)
            points.forEach { point ->
                lineTo(point.x, point.y)
            }
            lineTo(points.last().x, height - padding)
            close()
        }

        val gradient = Brush.verticalGradient(
            colors = listOf(
                lineColor.copy(alpha = 0.3f),
                lineColor.copy(alpha = 0.1f),
                Color.Transparent
            ),
            startY = 0f,
            endY = height
        )

        drawPath(
            path = path,
            brush = gradient
        )
    }

    // Draw line
    if (points.size > 1) {
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { point ->
                lineTo(point.x, point.y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    // Draw points
    points.forEach { point ->
        drawCircle(
            color = lineColor,
            radius = 6.dp.toPx(),
            center = point
        )
        drawCircle(
            color = backgroundColor,
            radius = 3.dp.toPx(),
            center = point
        )
    }
}
