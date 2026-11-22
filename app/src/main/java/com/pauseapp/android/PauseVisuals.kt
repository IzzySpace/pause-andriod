package com.pauseapp.android

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal val Ink = Color(0xFF221B39)
internal val Paper = Color(0xFFF9F6F0)
internal val Lime = Color(0xFFE9FAAB)
internal val Violet = Color(0xFF5A4292)
internal val Muted = Color(0xFF5E566C)
internal val Lavender = Color(0xFFECE5FA)
internal val Peach = Color(0xFFF9E9DF)
internal val Night = Color(0xFF211831)
internal val HeroBrush = Brush.linearGradient(listOf(Color(0xFF251A38), Color(0xFF44346E), Night))

@Composable
internal fun PauseWordmark() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(Modifier.size(38.dp).background(Ink, RoundedCornerShape(13.dp))) {
            val width = size.width * .105f
            val height = size.height * .40f
            val top = (size.height - height) / 2f
            for (fraction in listOf(.34f, .55f)) {
                drawRoundRect(Lime, Offset(size.width * fraction, top), Size(width, height), CornerRadius(width / 2f))
            }
        }
        Text("Pause", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun motionPhase(duration: Int, label: String): State<Float> =
    rememberInfiniteTransition(label = label).animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duration, easing = LinearEasing), RepeatMode.Restart),
        label = label,
    )

@Composable
internal fun OrbitArt(modifier: Modifier = Modifier) {
    val phase = motionPhase(14_000, "Orbit")
    Canvas(modifier) {
        val radius = size.minDimension / 2f
        val glowCenter = center + Offset(radius * .15f, -radius * .05f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF9365F1).copy(alpha = .58f), Color.Transparent),
                center = glowCenter,
                radius = radius * 1.5f,
            ),
            radius = radius * 1.5f,
            center = glowCenter,
        )
        val angle = phase.value * (2f * PI.toFloat())
        rotate(-24f) {
            repeat(3) { index ->
                val width = radius * (1.86f - index * .24f)
                val height = radius * (.72f + index * .23f)
                drawOval(
                    color = Color(0xFFCDB6FF).copy(alpha = .38f),
                    topLeft = center - Offset(width / 2f, height / 2f),
                    size = Size(width, height),
                    style = Stroke(1.dp.toPx()),
                )
            }
            val satellite = center + Offset(cos(angle) * radius * .93f, sin(angle) * radius * .36f)
            drawCircle(Lime.copy(alpha = .24f), 9.dp.toPx(), satellite)
            drawCircle(Lime, 3.2.dp.toPx(), satellite)
        }
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFF3EBFF), Color(0xFFB78BFA), Color(0xFF654396)),
                center = center - Offset(radius * .11f, radius * .17f),
                radius = radius * .67f,
            ),
            radius = radius * .31f,
        )
        drawCircle(Color.White.copy(alpha = .56f), 1.7.dp.toPx(), center + Offset(-radius * .63f, -radius * .45f))
        drawCircle(Lime.copy(alpha = .76f), 2.dp.toPx(), center + Offset(radius * .70f, radius * .45f))
    }
}

@Composable
internal fun BreathingRing(
    running: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val phase = if (running) motionPhase(8_000, "Breathing") else null
    Box(
        modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val radius = size.minDimension / 2f
            val breath = phase?.value?.let { (1f - cos(it * (2f * PI.toFloat()))) / 2f } ?: .60f
            val glow = radius * (.61f + breath * .22f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFBA96FF).copy(alpha = .50f), Color(0xFF9F74EE).copy(alpha = .30f), Color.Transparent),
                    center = center,
                    radius = glow,
                ),
                radius = glow,
            )
            drawCircle(Color(0xFFBDA2EF).copy(alpha = .24f), glow * .85f, style = Stroke(1.dp.toPx()))
            val inset = 10.dp.toPx()
            val span = size.minDimension - 2f * inset
            val topLeft = center - Offset(span / 2f, span / 2f)
            drawCircle(Color(0xFFCCB7F6).copy(alpha = .25f), span / 2f, style = Stroke(4.dp.toPx()))
            if (running) {
                drawArc(
                    color = Lime,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(span, span),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

@Composable
internal fun ActionIcon(isFilter: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val unit = size.width / 24f
        if (isFilter) {
            val shield = androidx.compose.ui.graphics.Path().apply {
                moveTo(12 * unit, 2 * unit)
                lineTo(21 * unit, 5.5f * unit)
                lineTo(21 * unit, 11 * unit)
                cubicTo(21 * unit, 16 * unit, 17 * unit, 19.5f * unit, 12 * unit, 22 * unit)
                cubicTo(7 * unit, 19.5f * unit, 3 * unit, 16 * unit, 3 * unit, 11 * unit)
                lineTo(3 * unit, 5.5f * unit)
                close()
            }
            drawPath(shield, Ink, style = stroke)
            drawLine(Ink, Offset(8.5f * unit, 12 * unit), Offset(11 * unit, 14.5f * unit), stroke.width, StrokeCap.Round)
            drawLine(Ink, Offset(11 * unit, 14.5f * unit), Offset(16 * unit, 9.5f * unit), stroke.width, StrokeCap.Round)
        } else {
            drawRoundRect(Ink, Offset(3 * unit, 3 * unit), Size(18 * unit, 18 * unit), CornerRadius(4 * unit), style = stroke)
            drawLine(Ink, Offset(7 * unit, 9 * unit), Offset(17 * unit, 9 * unit), stroke.width, StrokeCap.Round)
            drawLine(Ink, Offset(7 * unit, 14 * unit), Offset(14 * unit, 14 * unit), stroke.width, StrokeCap.Round)
        }
    }
}

@Composable
internal fun CanvasArrow() {
    Canvas(Modifier.size(20.dp)) {
        val weight = 1.8.dp.toPx()
        val reach = 3.5.dp.toPx()
        val tip = center + Offset(reach, 0f)
        drawLine(Ink, center + Offset(0f, -reach), tip, weight, StrokeCap.Round)
        drawLine(Ink, center + Offset(0f, reach), tip, weight, StrokeCap.Round)
        drawLine(Ink, center - Offset(reach * 1.5f, 0f), tip, weight, StrokeCap.Round)
    }
}
