package xyz.crearts.activebreak.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    title: String = "",
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
                    .size(200.dp),
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

        val total = data.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0) {
            Box(
                modifier = Modifier
                    .size(200.dp),
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
                .size(200.dp)
                .padding(16.dp)
        ) {
            drawPieChart(data, total)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(data) { item ->
                LegendItem(
                    color = item.color,
                    label = item.label,
                    value = item.value,
                    percentage = (item.value / total * 100).toInt(),
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    value: Float,
    percentage: Int,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape = CircleShape,
            color = color
        ) {}

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${value.toInt()} ($percentage%)",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

private fun DrawScope.drawPieChart(
    data: List<PieChartData>,
    total: Float
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = min(size.width, size.height) / 2 * 0.8f
    
    var startAngle = -90f // Start from top
    
    data.forEach { slice ->
        val sweepAngle = (slice.value / total) * 360f
        
        // Draw slice
        drawArc(
            color = slice.color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2)
        )
        
        // Draw slice border
        drawArc(
            color = Color.White,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        
        // Draw percentage text on slice if it's large enough
        if (sweepAngle > 30f) {
            val textAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val textRadius = radius * 0.7f
            val textX = center.x + cos(textAngle).toFloat() * textRadius
            val textY = center.y + sin(textAngle).toFloat() * textRadius
            
            val percentage = (slice.value / total * 100).toInt()
            if (percentage > 5) { // Only show percentage if it's more than 5%
                // Use Compose's drawIntoCanvas for native drawing
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 12.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                    canvas.nativeCanvas.drawText("$percentage%", textX, textY, paint)
                }
            }
        }
        
        startAngle += sweepAngle
    }
    
    // Draw center circle for donut effect
    drawCircle(
        color = Color.White,
        radius = radius * 0.4f,
        center = center
    )
}
