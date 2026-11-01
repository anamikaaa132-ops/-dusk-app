package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DuskDarkMoon
import com.example.ui.theme.DuskLightMoon
import kotlin.math.abs

val MOOD_LABELS = listOf("Rough", "Low", "Okay", "Good", "Great")

@Composable
fun MoonPhaseIcon(
    mood: Int, // 1..5
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    isSelected: Boolean = false,
    moonColor: Color = DuskLightMoon,
    rimColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
) {
    val f = when (mood) {
        1 -> 0.0f
        2 -> 0.25f
        3 -> 0.50f
        4 -> 0.75f
        5 -> 1.0f
        else -> 0.50f
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = (size.toPx() * 0.06f).coerceAtLeast(1.5f)
            val R = (this.size.minDimension - strokeWidth) / 2f
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f

            if (f == 1.0f) {
                // Full moon
                drawCircle(
                    color = moonColor,
                    radius = R,
                    center = center
                )
            } else if (f > 0.0f) {
                // Moon phase path
                val rx = R * abs(2f * f - 1f)
                val path = Path().apply {
                    // Start at top point
                    moveTo(cx, cy - R)
                    // Right semicircle from top to bottom
                    arcTo(
                        rect = Rect(cx - R, cy - R, cx + R, cy + R),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    // Now from bottom back to top via terminator
                    if (rx < 0.5f) {
                        // Half moon (straight vertical line)
                        lineTo(cx, cy - R)
                    } else if (f < 0.5f) {
                        // Crescent: terminator bulges right (sweep through 0° to -90°)
                        arcTo(
                            rect = Rect(cx - rx, cy - R, cx + rx, cy + R),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = -180f,
                            forceMoveTo = false
                        )
                    } else {
                        // Gibbous: terminator bulges left (sweep through 180° to 270°)
                        arcTo(
                            rect = Rect(cx - rx, cy - R, cx + rx, cy + R),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false
                        )
                    }
                    close()
                }
                drawPath(path = path, color = moonColor)
            }

            // Outer rim circle
            drawCircle(
                color = if (isSelected) moonColor.copy(alpha = 0.8f) else rimColor,
                radius = R,
                center = center,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
