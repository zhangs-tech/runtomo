package com.zhangs_tech.runtomo.summon

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp

/** A cream egg with spots and a brown outline, in the style of the Tomo art. Size it 4:5. */
@Composable
fun EggArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val egg = eggPath()
        drawPath(egg, EggShell)
        clipPath(egg) {
            spot(0.30f, 0.38f, 0.13f, EggSpotWarm)
            spot(0.70f, 0.30f, 0.08f, EggSpotGreen)
            spot(0.62f, 0.62f, 0.15f, EggSpotWarm)
            spot(0.24f, 0.72f, 0.08f, EggSpotGreen)
            // A highlight on the upper left, so the egg looks round.
            drawOval(EggHighlight, topLeft = Offset(size.width * 0.22f, size.height * 0.12f), size = Size(size.width * 0.14f, size.height * 0.16f))
        }
        drawPath(egg, EggOutline, style = Stroke(width = 2.5.dp.toPx()))
    }
}

private fun DrawScope.eggPath(): Path {
    val w = size.width
    val h = size.height
    val inset = 2.dp.toPx()
    return Path().apply {
        moveTo(w / 2, inset)
        cubicTo(w * 0.86f, inset, w - inset, h * 0.50f, w - inset, h * 0.64f)
        cubicTo(w - inset, h * 0.86f, w * 0.76f, h - inset, w / 2, h - inset)
        cubicTo(w * 0.24f, h - inset, inset, h * 0.86f, inset, h * 0.64f)
        cubicTo(inset, h * 0.50f, w * 0.14f, inset, w / 2, inset)
        close()
    }
}

private fun DrawScope.spot(x: Float, y: Float, radius: Float, color: Color) {
    drawCircle(color, radius = size.width * radius, center = Offset(size.width * x, size.height * y))
}

private val EggShell = Color(0xFFFFF4DC)
private val EggSpotWarm = Color(0xFFFFC98A)
private val EggSpotGreen = Color(0xFFC9D88F)
private val EggHighlight = Color(0xB3FFFFFF)
private val EggOutline = Color(0xFF6D3518)
