package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AnqeznyInk
import com.example.ui.theme.AnqeznyInkSecondary
import com.example.ui.theme.AnqeznyOutline

@Composable
fun SignaturePad(
    onSignaturePointsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val activeStroke = remember { mutableStateListOf<Offset>() }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "توقيع العميل الإلكتروني عند التسليم",
                style = MaterialTheme.typography.titleMedium,
                color = AnqeznyInk
            )
            TextButton(
                onClick = {
                    strokes.clear()
                    activeStroke.clear()
                    onSignaturePointsChanged(0)
                },
                modifier = Modifier.testTag("clear_signature_button")
            ) {
                Text("مسح التوقيع")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.5.dp, AnqeznyOutline, RoundedCornerShape(14.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { start ->
                            activeStroke.clear()
                            activeStroke.add(start)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            activeStroke.add(change.position)
                        },
                        onDragEnd = {
                            if (activeStroke.isNotEmpty()) {
                                strokes.add(activeStroke.toList())
                                activeStroke.clear()
                                val totalPoints = strokes.sumOf { it.size }
                                onSignaturePointsChanged(totalPoints)
                            }
                        }
                    )
                }
        ) {
            if (strokes.isEmpty() && activeStroke.isEmpty()) {
                Text(
                    text = "يرجى التوقيع بالإصبع داخل هذا المربع لتأكيد استلام السيارة سليمة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnqeznyInkSecondary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                fun drawStroke(pts: List<Offset>) {
                    if (pts.size < 2) return
                    val path = Path().apply {
                        moveTo(pts.first().x, pts.first().y)
                        for (i in 1 until pts.size) {
                            lineTo(pts[i].x, pts[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = AnqeznyInk,
                        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                strokes.forEach { drawStroke(it) }
                drawStroke(activeStroke)
            }
        }
    }
}
